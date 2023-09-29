package com.pratikk.jetpackpdf

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import com.pratikk.jetpackpdf.horizontalSamples.HorizontalPdfViewer
import com.pratikk.jetpackpdf.ui.theme.JetpackPDFTheme
import com.pratikk.jetpackpdf.verticalSamples.VerticalPdfViewer
import com.pratikk.jetpdfvue.state.VueFileType
import com.pratikk.jetpdfvue.state.VueResourceType
import com.pratikk.jetpdfvue.state.rememberHorizontalVueReaderState
import com.pratikk.jetpdfvue.state.rememberVerticalVueReaderState
import com.pratikk.jetpdfvue.util.toFile

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

                    HorizontalPreview()
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

