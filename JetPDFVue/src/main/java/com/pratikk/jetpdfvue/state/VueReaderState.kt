package com.pratikk.jetpdfvue.state

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.pratikk.jetpdfvue.VueRenderer
import com.pratikk.jetpdfvue.network.vueDownload
import com.pratikk.jetpdfvue.util.addImageToPdf
import com.pratikk.jetpdfvue.util.copyFile
import com.pratikk.jetpdfvue.util.generateFileName
import com.pratikk.jetpdfvue.util.mergePdf
import com.pratikk.jetpdfvue.util.share
import com.pratikk.jetpdfvue.util.toBase64File
import com.pratikk.jetpdfvue.util.toFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

abstract class VueReaderState(
    val vueResource: VueResourceType
) {
    abstract val TAG: String

    //View State
    var vueLoadState by mutableStateOf<VueLoadState>(VueLoadState.DocumentLoading)

    //Import State
    internal var vueImportState by mutableStateOf<VueImportState>(VueImportState.Ideal())

    //Document modified flag
    internal var mDocumentModified by mutableStateOf(false)
    val isDocumentModified
        get() = mDocumentModified

    //Import Job
    internal var importJob: Job? = null

    //Renderer
    internal var vueRenderer: VueRenderer? = null

    //Load with cache
    var cache: Int = 0

    //Container size
    internal var containerSize: IntSize? = null

    //Device orientation
    internal var isPortrait: Boolean = true

    //PDF File
    private var mFile by mutableStateOf<File?>(null)

    val file: File?
        get() = mFile

    internal var importFile: File? = null

    //Remote download status
    private var mLoadPercent by mutableStateOf(0)

    val loadPercent: Int
        get() = mLoadPercent

    val pdfPageCount: Int
        get() = vueRenderer?.pageCount ?: 0

    abstract val currentPage: Int

    abstract val isScrolling: Boolean
    abstract suspend fun nextPage()
    abstract suspend fun prevPage()

    abstract fun load(
        context: Context,
        coroutineScope: CoroutineScope,
        containerSize: IntSize,
        isPortrait: Boolean,
        customResource: (suspend CoroutineScope.() -> File)?
    )

    internal fun loadResource(
        context: Context,
        coroutineScope: CoroutineScope,
        loadCustomResource: (suspend CoroutineScope.() -> File)?
    ) {
        if (vueLoadState is VueLoadState.DocumentImporting) {
            require(vueResource is VueResourceType.Local || vueResource is VueResourceType.BlankDocument)
            if (vueResource is VueResourceType.Local)
                mFile = vueResource.uri.toFile()
            if (vueResource is VueResourceType.BlankDocument)
                mFile = vueResource.uri!!.toFile()
            requireNotNull(value = mFile, lazyMessage = { "Could not restore file" })
            return
        }

        vueLoadState = if (vueResource is VueResourceType.BlankDocument)
            VueLoadState.NoDocument
        else
            VueLoadState.DocumentLoading

        mLoadPercent = 0
        when (vueResource) {
            is VueResourceType.BlankDocument -> {
                coroutineScope.launch(Dispatchers.IO) {
                    runCatching {
                        val blankFile = File(context.filesDir, generateFileName())
                        mFile = vueResource.uri?.toFile() ?: blankFile
                    }.onFailure {
                        vueLoadState = VueLoadState.DocumentError(it)
                    }
                }
            }

            is VueResourceType.Asset -> {
                coroutineScope.launch(Dispatchers.IO) {
                    runCatching {
                        val inputStream = context.resources.openRawResource(vueResource.assetId)
                        mFile = when (vueResource.fileType) {
                            VueFileType.PDF -> {
                                val blankFile = File(context.filesDir, generateFileName())
                                inputStream.toFile("pdf").copyTo(blankFile, true)
                                blankFile
                            }

                            VueFileType.IMAGE -> {
                                val imgFile = inputStream.toFile("jpg")
                                val _file = File(context.filesDir, generateFileName())
                                addImageToPdf(
                                    imageFilePath = imgFile.absolutePath,
                                    pdfPath = _file.absolutePath
                                )
                                _file
                            }

                            VueFileType.BASE64 -> {
                                val blankFile = File(context.filesDir, generateFileName())
                                inputStream.toFile("txt").toBase64File().copyTo(blankFile, true)
                                blankFile
                            }
                        }
                        initRenderer()
                    }.onFailure {
                        vueLoadState = VueLoadState.DocumentError(it)
                    }
                }
            }

            is VueResourceType.Local -> {
                coroutineScope.launch(Dispatchers.IO) {
                    runCatching {
                        with(context) {
                            grantUriPermission(
                                packageName,
                                vueResource.uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )
                        }
                        mFile = when (vueResource.fileType) {
                            VueFileType.PDF -> {
                                val blankFile = File(context.filesDir, generateFileName())
                                if (vueResource.uri.scheme?.contains("content") == true) {
                                    //Create file from using input stream
                                    context.contentResolver.openInputStream(vueResource.uri)?.use {
                                        val type = context.contentResolver.getType(vueResource.uri)
                                            ?.split("/")?.get(1)
                                            ?: throw Throwable("Cannot decode uri file format")
                                        it.toFile(type).copyTo(blankFile, true)
                                    }
                                } else {
                                    vueResource.uri.toFile().copyTo(blankFile, true)
                                }
                                blankFile
                            }

                            VueFileType.IMAGE -> {
                                val blankFile = File(context.filesDir, generateFileName())
                                if (vueResource.uri.scheme?.contains("content") == true) {
                                    //Create file from using input stream
                                    context.contentResolver.openInputStream(vueResource.uri)?.use {
                                        val type = context.contentResolver.getType(vueResource.uri)
                                            ?.split("/")?.get(1)
                                            ?: throw Throwable("Cannot decode uri file format")
                                        it.toFile(type).copyTo(blankFile, true)
                                    }
                                } else {
                                    vueResource.uri.toFile().copyTo(blankFile, true)
                                }
                                val _file = File(context.filesDir, generateFileName())
                                addImageToPdf(
                                    imageFilePath = blankFile.absolutePath,
                                    pdfPath = _file.absolutePath
                                )
                                _file
                            }

                            VueFileType.BASE64 -> {
                                val blankFile = File(context.filesDir, generateFileName())
                                if (vueResource.uri.scheme?.contains("content") == true) {
                                    //Create file from using input stream
                                    context.contentResolver.openInputStream(vueResource.uri)?.use {
                                        val type = context.contentResolver.getType(vueResource.uri)
                                            ?.split("/")?.get(1)
                                            ?: throw Throwable("Cannot decode uri file format")
                                        it.toFile(type).toBase64File().copyTo(blankFile, true)
                                    }
                                } else {
                                    vueResource.uri.toFile().toBase64File().copyTo(blankFile, true)
                                }
                                blankFile
                            }
                        }

                        initRenderer()
                    }.onFailure {
                        vueLoadState = VueLoadState.DocumentError(it)
                    }
                }
            }

            is VueResourceType.Remote -> {
                coroutineScope.launch(Dispatchers.IO) {
                    runCatching {
                        val _file = File(context.filesDir, generateFileName())
                        vueDownload(
                            vueResource,
                            _file,
                            onProgressChange = { progress ->
                                mLoadPercent = progress
                            },
                            onSuccess = {
                                mFile = _file
                                initRenderer()
                            }
                        ) {
                            throw it
                        }
                    }.onFailure {
                        vueLoadState = VueLoadState.DocumentError(it)
                    }
                }
            }

            is VueResourceType.Custom -> {
                coroutineScope.launch(Dispatchers.IO) {
                    runCatching {
                        requireNotNull(loadCustomResource,
                            lazyMessage = { "Custom resource method cannot be null" })
                        val customFile = loadCustomResource()
                        val _file = File(context.filesDir, generateFileName())
                        customFile.copyTo(_file, true)
                        mFile = _file
                        initRenderer()
                    }.onFailure {
                        vueLoadState = VueLoadState.DocumentError(it)
                    }
                }
            }
        }
    }

    private fun initRenderer() {
        requireNotNull(containerSize)
        vueRenderer = VueRenderer(
            fileDescriptor = ParcelFileDescriptor.open(
                file,
                ParcelFileDescriptor.MODE_READ_ONLY
            ),
            containerSize = containerSize!!,
            isPortrait = isPortrait,
            cache = cache
        )
        vueLoadState = VueLoadState.DocumentLoaded
    }

    /**
     * Helper to launch import intent
     * @param vueImportSources At least one source is required. If base64 and pdf both are included then
     * the file manager will enable importing of other file types as well.
     * */
    fun launchImportIntent(
        context: Context,
        vueImportSources: List<VueImportSources> = listOf(
            VueImportSources.CAMERA,
            VueImportSources.GALLERY,
            VueImportSources.PDF
        ),
        launcher: ActivityResultLauncher<Intent>
    ) {
        require(
            value = vueImportSources.isNotEmpty(),
            lazyMessage = { "File Sources cannot be empty" })
        val intents = ArrayList<Intent>()
        val filterImportState = vueImportSources.toMutableList().let {
            if (it.contains(VueImportSources.BASE64) && it.contains(VueImportSources.PDF) && it.contains(
                    VueImportSources.GALLERY
                )
            ) {
                it.remove(VueImportSources.PDF)
                it.remove(VueImportSources.BASE64)
                intents.add(base64PdfAndGalleryIntent())
            } else if (it.contains(VueImportSources.BASE64) && it.contains(VueImportSources.PDF)) {
                it.remove(VueImportSources.PDF)
                it.remove(VueImportSources.BASE64)
                intents.add(base64AndPdfIntent())
            } else if (it.contains(VueImportSources.PDF) && it.contains(VueImportSources.GALLERY)) {
                it.remove(VueImportSources.PDF)
                intents.add(pdfAndGalleryIntent())
            }
            it
        }
        filterImportState.forEach { source ->
            val intent = when (source) {
                VueImportSources.CAMERA -> cameraIntent(context)
                VueImportSources.GALLERY -> galleryIntent()
                VueImportSources.BASE64 -> base64Intent()
                VueImportSources.PDF -> pdfIntent()
            }
            intents.add(intent)
        }
        val chooserIntent = createChooserIntent(intents)
        vueLoadState = VueLoadState.DocumentImporting
        vueRenderer?.close()
        launcher.launch(chooserIntent)
    }

    /**
     * Intent launcher for importing
     * */
    @Composable
    fun getImportLauncher(interceptResult: suspend (File) -> Unit = {}): ActivityResultLauncher<Intent> {
        val context = LocalContext.current
        LaunchedEffect(vueImportState) {
            if (vueImportState is VueImportState.Imported)
                if (importJob == null) {
                    importJob = launch(
                        context = coroutineContext + Dispatchers.IO,
                        start = CoroutineStart.LAZY
                    ) {
                        runCatching {
                            val uri = (vueImportState as VueImportState.Imported).uri
                            //If uri is null then the result is from camera , otherwise its from gallery
                            if (uri != null && context.contentResolver.getType(uri)
                                    ?.contains("pdf") == true
                            ) {
                                // First try for content scheme otherwise file scheme
                                val importedPdf =
                                    context.contentResolver.openInputStream(uri)?.use {
                                        it.toFile("pdf")
                                    } ?: uri.toFile()
                                //If there is no existing pdf and no pdf for that document then copy imported doc to downloadDocumentFile
                                if (file != null && !file!!.exists() && file!!.length() == 0L) {
                                    importedPdf.copyTo(file!!, true)
                                } else {
                                    //Merge imported pdf with existing pdf
                                    mergePdf(
                                        oldPdfPath = file!!.absolutePath,
                                        importedPdfPath = importedPdf.absolutePath
                                    )
                                }
                            } else {
                                requireNotNull(
                                    value = importFile,
                                    lazyMessage = { "Import file cannot be null" })
                                uri?.copyFile(context, importFile!!.toUri())
                                //Give option to user to manipulate result like compress or rotate
                                if (isActive)
                                    interceptResult(importFile!!)
                                addImageToPdf(
                                    imageFilePath = importFile!!.absolutePath,
                                    pdfPath = file!!.absolutePath
                                )
                            }
                            if (isActive) {
                                initRenderer()
                                vueImportState = VueImportState.Ideal()
                                mDocumentModified = true
                                importFile = null
                                importJob = null
                            }
                        }.onFailure {
                            if (isActive) {
                                vueLoadState = VueLoadState.DocumentError(it)
                                importFile = null
                                vueImportState = VueImportState.Ideal()
                                importJob = null
                            }
                        }
                    }
                    importJob?.start()
                    importJob?.join()
                }
        }
        return rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
            onResult = {
                if (it.resultCode == RESULT_OK) {
                    val uri = it.data?.data
                    vueImportState = VueImportState.Imported(uri)
                } else {
                    if (vueResource !is VueResourceType.BlankDocument)
                        initRenderer()
                    else
                        vueLoadState = VueLoadState.NoDocument
                }
            }
        )
    }

    /**
     * Helper intent creator for importing pdf or image from gallery/camera
     */
    private fun createChooserIntent(intents: ArrayList<Intent>): Intent {
        val chooserIntent = Intent.createChooser(Intent(), "Select action")
        chooserIntent.putExtra(Intent.EXTRA_INTENT, intents[0])
        if (intents.size > 1) {
            intents.removeAt(0)
            chooserIntent.putExtra(
                Intent.EXTRA_INITIAL_INTENTS,
                intents.toTypedArray()
            )
        }
        return chooserIntent
    }

    private fun cameraIntent(context: Context): Intent {
        importFile = File(context.cacheDir, "importTemp_${System.currentTimeMillis()}.jpg")
        if (importFile?.parentFile?.exists() == false) {
            importFile?.parentFile?.mkdirs()
        } else {
            importFile?.parentFile?.deleteRecursively()
            importFile?.parentFile?.mkdirs()
        }
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            val uri: Uri = if (Build.VERSION.SDK_INT < 24)
                Uri.fromFile(importFile)
            else
                FileProvider.getUriForFile(
                    context,
                    context.applicationContext.packageName + ".provider",
                    importFile!!
                )
            putExtra(MediaStore.EXTRA_OUTPUT, uri)
        }
        return cameraIntent
    }

    private fun galleryIntent(): Intent {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        return intent
    }

    private fun base64Intent(): Intent {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "text/plain"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        return intent
    }

    private fun pdfIntent(): Intent {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/pdf"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        return intent
    }

    private fun base64PdfAndGalleryIntent(): Intent {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.putExtra(
            Intent.EXTRA_MIME_TYPES,
            listOf("application/pdf", "text/plain", "image/*").toTypedArray()
        )
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        return intent
    }

    private fun base64AndPdfIntent(): Intent {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.putExtra(
            Intent.EXTRA_MIME_TYPES,
            listOf("application/pdf", "text/plain").toTypedArray()
        )
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        return intent
    }

    private fun pdfAndGalleryIntent(): Intent {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.putExtra(
            Intent.EXTRA_MIME_TYPES,
            listOf("application/pdf", "image/*").toTypedArray()
        )
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        return intent
    }

    fun sharePDF(context: Context) {
        file?.share(context)
    }

    abstract fun rotate(angle: Float)

    internal fun getResourceType() =
        when (vueResource) {
            is VueResourceType.Asset -> vueResource.fileType
            is VueResourceType.Local -> vueResource.fileType
            is VueResourceType.Remote -> vueResource.fileType
            is VueResourceType.BlankDocument,
            VueResourceType.Custom -> VueFileType.PDF
        }
}