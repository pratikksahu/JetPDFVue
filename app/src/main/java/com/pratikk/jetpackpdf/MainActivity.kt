package com.pratikk.jetpackpdf

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.pratikk.jetpackpdf.horizontalSamples.HorizontalPdfViewer
import com.pratikk.jetpackpdf.horizontalSamples.HorizontalSampleA
import com.pratikk.jetpackpdf.horizontalSamples.HorizontalSampleB
import com.pratikk.jetpackpdf.ui.theme.JetpackPDFTheme
import com.pratikk.jetpackpdf.verticalSamples.VerticalPdfViewer
import com.pratikk.jetpackpdf.verticalSamples.VerticalSampleA
import com.pratikk.jetpdfvue.HorizontalVueReader
import com.pratikk.jetpdfvue.VerticalVueReader
import com.pratikk.jetpdfvue.VueHorizontalSlider
import com.pratikk.jetpdfvue.state.HorizontalVueReaderState
import com.pratikk.jetpdfvue.state.VerticalVueReaderState
import com.pratikk.jetpdfvue.state.VueLoadState
import com.pratikk.jetpdfvue.state.VueResourceType
import com.pratikk.jetpdfvue.state.rememberHorizontalVueReaderState
import com.pratikk.jetpdfvue.state.rememberVerticalVueReaderState
import com.pratikk.jetpdfvue.toFile
import com.pratikk.jetpdfvue.util.reduceSize
import kotlinx.coroutines.launch

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
    val assetReader = rememberHorizontalVueReaderState(
        resource = VueResourceType.Asset(R.raw.lorem_ipsum)
    )
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
    HorizontalPdfViewer(horizontalVueReaderState = assetReader)
}
@Composable
fun VerticalPreview(){
    val context = LocalContext.current
    val assetReader = rememberVerticalVueReaderState(
        resource = VueResourceType.Asset(R.raw.lorem_ipsum)
    )
    val localBase64Reader = rememberVerticalVueReaderState(
        resource = VueResourceType.Base64(
            context.assets.open("lorem_ipsum_base64.txt").let { inputStream ->
                inputStream.toFile(extension = ".txt")
            }
        )
    )
    val remoteBase64Reader =
        rememberVerticalVueReaderState(resource = VueResourceType.RemoteBase64("https://drive.google.com/uc?export=download&id=1-mmdJ2K2x3MDgTqmFd8sMpW3zIFyNYY-"))
    val remoteReader =
        rememberVerticalVueReaderState(resource = VueResourceType.Remote("https://drive.google.com/uc?export=download&id=1DSA7cmFzqCtTsHhlB0xdYJ6UweuC8IOz"))

    val remoteImageLink = listOf("https://images.pexels.com/photos/943907/pexels-photo-943907.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2","https://images.freeimages.com/images/large-previews/7f3/path-1441068.jpg")
    val remoteImage =
        rememberVerticalVueReaderState(resource = VueResourceType.Remote(remoteImageLink[0]))
    VerticalPdfViewer(verticalVueReaderState = remoteImage)
}

