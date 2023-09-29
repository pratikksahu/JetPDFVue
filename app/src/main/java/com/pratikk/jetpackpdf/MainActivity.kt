package com.pratikk.jetpackpdf

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import com.pratikk.jetpackpdf.horizontalSamples.HorizontalPdfViewer
import com.pratikk.jetpackpdf.ui.theme.JetpackPDFTheme
import com.pratikk.jetpackpdf.verticalSamples.VerticalPdfViewer
import com.pratikk.jetpdfvue.state.VueFilePicker
import com.pratikk.jetpdfvue.state.VueFilePickerState
import com.pratikk.jetpdfvue.state.VueFileSources
import com.pratikk.jetpdfvue.state.VueFileType
import com.pratikk.jetpdfvue.state.VueResourceType
import com.pratikk.jetpdfvue.state.rememberHorizontalVueReaderState
import com.pratikk.jetpdfvue.state.rememberVerticalVueReaderState
import com.pratikk.jetpdfvue.util.toFile
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JetpackPDFTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    val vueFilePicker = rememberSaveable(saver = VueFilePicker.Saver) {
                        VueFilePicker()
                    }
                    val launcher = vueFilePicker.getLauncher()
                    when(vueFilePicker.vueFilePickerState){
                        VueFilePickerState.VueFilePickerIdeal -> {
                            Column(modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center) {
                                Button(onClick = { vueFilePicker.launchIntent(context, listOf(VueFileSources.CAMERA,VueFileSources.GALLERY,VueFileSources.PDF),launcher)}) {
                                    Text(text = "Import Me")
                                }
                            }
                        }
                        is VueFilePickerState.VueFilePickerImported -> {
                            val localImage = rememberHorizontalVueReaderState(
                                resource = VueResourceType.Local(
                                    uri = (vueFilePicker.vueFilePickerState as VueFilePickerState.VueFilePickerImported).file.toUri(),
                                    fileType = VueFileType.IMAGE
                                )
                            )
                            HorizontalPdfViewer(horizontalVueReaderState = localImage)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HorizontalPreview(){
    val context = LocalContext.current
    val blankReader = rememberHorizontalVueReaderState(
        resource = VueResourceType.BlankDocument())
    val localImage = rememberHorizontalVueReaderState(
        resource = VueResourceType.Local(
            uri = context.resources.openRawResource(
                R.raw.demo
            ).toFile(".jpg").toUri(),
            fileType = VueFileType.IMAGE
        ),
    )
    val localPdf = rememberHorizontalVueReaderState(
        resource = VueResourceType.Local(
            uri = context.resources.openRawResource(
                R.raw.lorem_ipsum
            ).toFile(".pdf").toUri(),
            fileType = VueFileType.PDF
        )
    )
    val localBase64 = rememberHorizontalVueReaderState(
        resource = VueResourceType.Local(
            uri = context.resources.openRawResource(
                R.raw.lorem_ipsum_base64
            ).toFile(".txt").toUri(),
            fileType = VueFileType.BASE64
        )
    )
    val assetImage = rememberHorizontalVueReaderState(
        resource = VueResourceType.Asset(assetId = R.raw.demo,fileType = VueFileType.IMAGE))

    val assetPdf = rememberHorizontalVueReaderState(
        resource = VueResourceType.Asset(assetId = R.raw.lorem_ipsum,fileType = VueFileType.PDF))

    val assetBase64 = rememberHorizontalVueReaderState(
        resource = VueResourceType.Asset(assetId = R.raw.lorem_ipsum_base64,fileType = VueFileType.BASE64))

    val remoteImageLink = listOf("https://images.pexels.com/photos/943907/pexels-photo-943907.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2","https://images.freeimages.com/images/large-previews/7f3/path-1441068.jpg")

    val remoteImage =
        rememberHorizontalVueReaderState(resource = VueResourceType.Remote(remoteImageLink[0], fileType = VueFileType.IMAGE))

    val remotePdf =
        rememberHorizontalVueReaderState(resource = VueResourceType.Remote("https://drive.google.com/uc?export=download&id=1DSA7cmFzqCtTsHhlB0xdYJ6UweuC8IOz", fileType = VueFileType.PDF))

    val remoteBase64 =
        rememberHorizontalVueReaderState(resource = VueResourceType.Remote("https://drive.google.com/uc?export=download&id=1-mmdJ2K2x3MDgTqmFd8sMpW3zIFyNYY-", fileType = VueFileType.BASE64))

    HorizontalPdfViewer(horizontalVueReaderState = blankReader)
}
@Composable
fun VerticalPreview(){
    val context = LocalContext.current
    val blankReader = rememberVerticalVueReaderState(
        resource = VueResourceType.BlankDocument())
    val localImage = rememberVerticalVueReaderState(
        resource = VueResourceType.Local(
            uri = context.resources.openRawResource(
                R.raw.demo
            ).toFile(".jpg").toUri(),
            fileType = VueFileType.IMAGE
        ),
    )
    val localPdf = rememberVerticalVueReaderState(
        resource = VueResourceType.Local(
            uri = context.resources.openRawResource(
                R.raw.lorem_ipsum
            ).toFile(".pdf").toUri(),
            fileType = VueFileType.PDF
        )
    )
    val assetImage = rememberVerticalVueReaderState(
        resource = VueResourceType.Asset(assetId = R.raw.demo,fileType = VueFileType.IMAGE))

    val assetPdf = rememberVerticalVueReaderState(
        resource = VueResourceType.Asset(assetId = R.raw.lorem_ipsum,fileType = VueFileType.PDF))

    val assetBase64 = rememberVerticalVueReaderState(
        resource = VueResourceType.Asset(assetId = R.raw.lorem_ipsum_base64,fileType = VueFileType.BASE64))

    val remoteImageLink = listOf("https://images.pexels.com/photos/943907/pexels-photo-943907.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2","https://images.freeimages.com/images/large-previews/7f3/path-1441068.jpg")

    val remoteImage =
        rememberVerticalVueReaderState(resource = VueResourceType.Remote(remoteImageLink[0], fileType = VueFileType.IMAGE))

    val remotePdf =
        rememberVerticalVueReaderState(resource = VueResourceType.Remote("https://drive.google.com/uc?export=download&id=1DSA7cmFzqCtTsHhlB0xdYJ6UweuC8IOz", fileType = VueFileType.PDF))

    val remoteBase64 =
        rememberVerticalVueReaderState(resource = VueResourceType.Remote("https://drive.google.com/uc?export=download&id=1-mmdJ2K2x3MDgTqmFd8sMpW3zIFyNYY-", fileType = VueFileType.BASE64))

    VerticalPdfViewer(verticalVueReaderState = localImage)
}

