package com.pratikk.jetpdfvue.state

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.pratikk.jetpdfvue.util.generateFileName
import com.pratikk.jetpdfvue.util.getDateddMMyyyyHHmm
import com.pratikk.jetpdfvue.util.getFile
import com.pratikk.jetpdfvue.util.toFile
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import java.io.File
import java.util.Calendar

sealed class VueFilePickerState {
    @Parcelize
    data class VueFilePickerImported(val uri: Uri) : VueFilePickerState(), Parcelable {
        fun getFileType(context: Context): VueFileType {
            val type = context.contentResolver.getType(uri)
                ?: uri.scheme ?: throw Throwable("File type cannot be decoded, please check uri $uri")
            return if (type.contains("pdf"))
                VueFileType.PDF
            else if (type.contains("text") || type.contains("txt"))
                VueFileType.BASE64
            else
                VueFileType.IMAGE
        }
    }

        @Parcelize
        data object VueFilePickerIdeal : VueFilePickerState(), Parcelable
    }

enum class VueImportSources {
    CAMERA, GALLERY, BASE64, PDF
}

class VueFilePicker {
    private var importFile: File? = null
    private var importJob: Job? = null
    var vueFilePickerState by mutableStateOf<VueFilePickerState>(VueFilePickerState.VueFilePickerIdeal)
        private set

    companion object {
        val Saver: Saver<VueFilePicker, *> = listSaver(
            save = {
                it.importJob?.cancel()
                buildList {
                    add(it.importFile?.absolutePath)
                    add(it.vueFilePickerState)
                }.toList()
            },
            restore = {
                VueFilePicker().apply {
                    importFile = (it[0] as String?)?.let { path ->
                        File(path)
                    }
                    vueFilePickerState = it[1] as VueFilePickerState
                }
            }
        )
    }

    /**
     * @param vueImportSources At least one source is required. If base64 and pdf both are included then
     * the file manager will enable importing of other file types as well.
     * */
    fun launchIntent(
        context: Context,
        vueImportSources: List<VueImportSources>,
        launcher: ManagedActivityResultLauncher<Intent, ActivityResult>
    ) {
        require(
            value = vueImportSources.isNotEmpty(),
            lazyMessage = { "File Sources cannot be empty" })
        val intents = ArrayList<Intent>()
        val filterImportState = vueImportSources.toMutableList().let {
            if(it.contains(VueImportSources.BASE64) && it.contains(VueImportSources.PDF)) {
                it.remove(VueImportSources.PDF)
                it.remove(VueImportSources.BASE64)
                intents.add(base64AndPdfIntent())
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
        launcher.launch(chooserIntent)
    }

    @Composable
    fun getLauncher(onResult: (Uri) -> Unit = {}): ManagedActivityResultLauncher<Intent, ActivityResult> {
        return rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val uri = it.data?.data
                if (uri != null) {
                    //Other sources
                    vueFilePickerState = VueFilePickerState.VueFilePickerImported(uri)
                    onResult(uri)
                } else {
                    //From Camera
                    vueFilePickerState = VueFilePickerState.VueFilePickerImported(importFile!!.toUri())
                    onResult(importFile!!.toUri())
                }
            } else {
                vueFilePickerState = VueFilePickerState.VueFilePickerIdeal
            }
        }
    }

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
        importFile = File(
            context.filesDir,
            "${Calendar.getInstance().timeInMillis.getDateddMMyyyyHHmm()}_${Calendar.getInstance().timeInMillis}.jpg"
        )
        if (importFile?.parentFile?.exists() == false) {
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
    private fun base64AndPdfIntent():Intent{
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, listOf("application/pdf","text/plain").toTypedArray())
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        return intent
    }
}