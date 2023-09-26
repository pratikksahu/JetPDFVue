package com.pratikk.jetpdfvue.state

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
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
import com.pratikk.jetpdfvue.util.addImageToPdf
import com.pratikk.jetpdfvue.util.copyFile
import com.pratikk.jetpdfvue.generateFileName
import com.pratikk.jetpdfvue.util.getFile
import com.pratikk.jetpdfvue.util.mergePdf
import com.pratikk.jetpdfvue.network.VueDownload
import com.pratikk.jetpdfvue.util.share
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

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
    internal var importJob:Job? = null
    //Renderer
    internal var vueRenderer: VueRenderer? = null

    //Container size
    internal var containerSize: IntSize? = null

    //Device orientation
    internal var isPortrait:Boolean = true

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
        isPortrait:Boolean,
        customResource: (suspend CoroutineScope.() -> File)?
    )

    fun loadResource(
        context: Context,
        coroutineScope: CoroutineScope,
        loadCustomResource: (suspend CoroutineScope.() -> File)?
    ) {
        if (vueLoadState is VueLoadState.DocumentImporting) {
            require(vueResource is VueResourceType.Local)
                mFile = vueResource.uri.toFile()
            Log.d(TAG,"Cannot load, importing document")
            return
        }
        vueLoadState = VueLoadState.DocumentLoading
        mLoadPercent = 0
        when (vueResource) {
            is VueResourceType.Asset -> {
                coroutineScope.launch(Dispatchers.IO) {
                    runCatching {
                        val bufferSize = 8192
                        val inputStream = context.resources.openRawResource(vueResource.assetId)
                        val outFile = File(context.cacheDir, generateFileName())
                        inputStream.use { input ->
                            outFile.outputStream().use { output ->
                                var data = ByteArray(bufferSize)
                                var count = input.read(data)
                                while (count != -1) {
                                    output.write(data, 0, count)
                                    data = ByteArray(bufferSize)
                                    count = input.read(data)
                                }
                            }
                        }
                        mFile = file
                        initRenderer()
                    }.onFailure {
                        vueLoadState = VueLoadState.DocumentError(it)
                    }
                }
            }

            is VueResourceType.Base64 -> {
                coroutineScope.launch(Dispatchers.IO) {
                    runCatching {
                        val _file = File(context.filesDir, generateFileName())
                        with(FileOutputStream(_file, false)) {
                            withContext(Dispatchers.IO) {
                                write(Base64.decode(vueResource.file, Base64.DEFAULT))
                                flush()
                                close()
                            }
                        }
                        mFile = _file
                        initRenderer()
                    }.onFailure {
                        vueLoadState = VueLoadState.DocumentError(it)
                    }
                }
            }

            is VueResourceType.Local -> {
                coroutineScope.launch(Dispatchers.IO) {
                    runCatching {
                        mFile = vueResource.uri.toFile()
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
                        VueDownload(
                            vueResource,
                            _file,
                            onProgressChange = { progress ->
                                mLoadPercent = progress
                            },
                            onSuccess = {
                                mFile = _file
                                initRenderer()
                            },
                            onError = {
                                throw it
                            })
                    }.onFailure {
                        vueLoadState = VueLoadState.DocumentError(it)
                    }
                }
            }

            is VueResourceType.RemoteBase64 -> {
                coroutineScope.launch(Dispatchers.IO) {
                    runCatching {
                        val _file = File(context.filesDir, generateFileName())
                        VueDownload(
                            vueResource,
                            _file,
                            onProgressChange = { progress ->
                                mLoadPercent = progress
                            },
                            onSuccess = {
                                mFile = _file
                                initRenderer()
                            },
                            onError = {
                                throw it
                            })
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
            isPortrait = isPortrait
        )
        vueLoadState = VueLoadState.DocumentLoaded
    }

    /**
     * Should launch import intent by invoking this function
     * */
    fun launchImportIntent(context: Context,launcher:ActivityResultLauncher<Intent>){
        val intent = getIntentForImporting(context = context)
        vueLoadState = VueLoadState.DocumentImporting
        vueRenderer?.close()
        launcher.launch(intent)
    }

    /**
     * Intent launcher for importing
     * */
    @Composable
    fun getImportLauncher(interceptResult: suspend (File) -> Unit = {}): ActivityResultLauncher<Intent> {
        val context = LocalContext.current
        LaunchedEffect(vueImportState){
            Log.d(TAG,vueImportState.toString())
            if(vueImportState is VueImportState.Imported)
                handleImportResult(
                    uri = (vueImportState as VueImportState.Imported).uri,
                    scope = this,
                    interceptResult = interceptResult,
                    context = context)
        }
        return rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
            onResult = {
                Log.d(TAG, "Result ${it}")
                if (it.resultCode == RESULT_OK) {
                    val uri = it.data?.data
                    vueImportState = VueImportState.Imported(uri)
                } else {
                    initRenderer()
                }
            }
        )
    }

    /**
     * Helper intent creator for importing pdf or image from gallery/camera
     */
    private fun getIntentForImporting(context: Context): Intent {
        importFile = File(context.cacheDir, "importTemp_${System.currentTimeMillis()}.jpg")
        if (importFile?.parentFile?.exists() == false) {
            importFile?.parentFile?.mkdirs()
        }else{
            importFile?.parentFile?.deleteRecursively()
            importFile?.parentFile?.mkdirs()
        }

        val mimeTypes = arrayListOf("image/*", "application/pdf")
        val galleryAndPdfIntent = Intent(Intent.ACTION_GET_CONTENT)
        galleryAndPdfIntent.type = "*/*"
        galleryAndPdfIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes.joinToString("|"))

        // Camera
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val uri: Uri = if (Build.VERSION.SDK_INT < 24)
            Uri.fromFile(importFile)
        else
            FileProvider.getUriForFile(
                context,
                context.applicationContext.packageName + ".provider",
                importFile!!
            )
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)

        // Chooser of filesystem options.
        val chooserIntent = Intent(Intent.ACTION_CHOOSER)
        chooserIntent.putExtra(Intent.EXTRA_INTENT, galleryAndPdfIntent)

//         Add the camera options.
        chooserIntent.putExtra(
            Intent.EXTRA_INITIAL_INTENTS,
            arrayOf(cameraIntent)
        )
        return chooserIntent
    }

    /**
     * @param interceptResult Can be used to do operation on the imported image/pdf. Changes must be saved to the same file object
     * */
    private fun handleImportResult(
        uri: Uri?,
        scope: CoroutineScope,
        interceptResult:suspend (File) -> Unit = {},
        context: Context
    ) {
        if (importJob == null)
            scope.launch(context = Dispatchers.IO) {
                importJob = launch(context = coroutineContext,start = CoroutineStart.LAZY) {
                    runCatching {
                        Log.d(TAG, "Received data")
                        //If uri is null then the result is from camera , otherwise its from gallery
                        if (uri != null && context.contentResolver.getType(uri)
                                ?.contains("pdf") == true
                        ) {
                            val importedPdf = uri.getFile(context)
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
                                lazyMessage = {"Import file cannot be null"})
                            uri?.copyFile(context,importFile!!.toUri())
                            //Give option to user to manipulate result like compress or rotate
                            if(isActive)
                                interceptResult(importFile!!)
                            addImageToPdf(
                                imageFilePath = importFile!!.absolutePath,
                                pdfPath = file!!.absolutePath
                            )
                        }
                        if(isActive) {
                            initRenderer()
                            vueImportState = VueImportState.Ideal()
                            mDocumentModified = true
                            importFile = null
                            importJob = null
                        }
                    }.onFailure {
                        if(isActive) {
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

    fun sharePDF(context: Context){
        file?.share(context)
    }

    abstract fun rotate(angle: Float)
}