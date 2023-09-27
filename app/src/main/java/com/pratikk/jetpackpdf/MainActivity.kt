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
import com.pratikk.jetpackpdf.horizontalSamples.HorizontalPdfViewer
import com.pratikk.jetpackpdf.ui.theme.JetpackPDFTheme
import com.pratikk.jetpackpdf.verticalSamples.VerticalPdfViewer
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

    val assetReader = rememberHorizontalVueReaderState(
        resource = VueResourceType.Asset(R.raw.lorem_ipsum))
    val localBase64Reader = rememberHorizontalVueReaderState(
        resource = VueResourceType.Base64(
            context.assets.open("lorem_ipsum_base64.txt").let { inputStream ->
                inputStream.toFile(extension = ".txt")
            }
        )
    )
    val remoteBase64Reader =
        rememberHorizontalVueReaderState(resource = VueResourceType.RemoteBase64("https://drive.google.com/uc?export=download&id=1-mmdJ2K2x3MDgTqmFd8sMpW3zIFyNYY-"))
    val remoteReader =
        rememberHorizontalVueReaderState(resource = VueResourceType.Remote("https://drive.google.com/uc?export=download&id=1DSA7cmFzqCtTsHhlB0xdYJ6UweuC8IOz"))

    val remoteImageLink = listOf("https://images.pexels.com/photos/943907/pexels-photo-943907.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2","https://images.freeimages.com/images/large-previews/7f3/path-1441068.jpg")
    val remoteImage =
        rememberHorizontalVueReaderState(resource = VueResourceType.Remote(remoteImageLink[0]))
    HorizontalPdfViewer(horizontalVueReaderState = blankReader)
}
@Composable
fun VerticalPreview(){
    val context = LocalContext.current
    val assetReader = rememberVerticalVueReaderState(
        resource = VueResourceType.Asset(R.raw.lorem_ipsum),
        cache = 3
    )
    val localBase64Reader = rememberVerticalVueReaderState(
        resource = VueResourceType.Base64(
            context.assets.open("lorem_ipsum_base64.txt").let { inputStream ->
                inputStream.toFile(extension = ".txt")
            }
        ),
        cache = 3
    )
    val remoteBase64Reader =
        rememberVerticalVueReaderState(resource = VueResourceType.RemoteBase64("https://drive.google.com/uc?export=download&id=1-mmdJ2K2x3MDgTqmFd8sMpW3zIFyNYY-"))
    val remoteReader =
        rememberVerticalVueReaderState(resource = VueResourceType.Remote("https://drive.google.com/uc?export=download&id=1DSA7cmFzqCtTsHhlB0xdYJ6UweuC8IOz"))

    val remoteImageLink = listOf("https://images.pexels.com/photos/943907/pexels-photo-943907.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2","https://images.freeimages.com/images/large-previews/7f3/path-1441068.jpg")
    val remoteImage =
        rememberVerticalVueReaderState(resource = VueResourceType.Remote(remoteImageLink[0]))
    VerticalPdfViewer(verticalVueReaderState = assetReader)
}

