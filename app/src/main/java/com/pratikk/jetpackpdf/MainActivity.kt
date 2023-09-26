package com.pratikk.jetpackpdf

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.pratikk.jetpackpdf.ui.theme.JetpackPDFTheme
import com.pratikk.jetpdfvue.HorizontalVueReader
import com.pratikk.jetpdfvue.VerticalVueReader
import com.pratikk.jetpdfvue.VueHorizontalSlider
import com.pratikk.jetpdfvue.state.VueLoadState
import com.pratikk.jetpdfvue.state.VueResourceType
import com.pratikk.jetpdfvue.state.rememberHorizontalVueReaderState
import com.pratikk.jetpdfvue.state.rememberVerticalVueReaderState
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
                    HorizontalPdfViewer()
                }
            }
        }
    }
}

@Composable
fun HorizontalPdfViewer() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val horizontalVueReaderState = rememberHorizontalVueReaderState(
        resource = VueResourceType.Remote(
            "https://myreport.altervista.org/Lorem_Ipsum.pdf"
        )
    )
    val launcher = horizontalVueReaderState.getImportLauncher(interceptResult = {
        it.reduceSize()
    })

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val configuration = LocalConfiguration.current
        val containerSize = remember {
            IntSize(constraints.maxWidth, constraints.maxHeight)
        }

        LaunchedEffect(Unit) {
            horizontalVueReaderState.load(
                context = context,
                coroutineScope = scope,
                containerSize = containerSize,
                isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT,
                customResource = null
            )
        }
        when (horizontalVueReaderState.vueLoadState) {
            is VueLoadState.DocumentError -> {
                Column {
                    Text(text = "Error:  ${horizontalVueReaderState.vueLoadState.getErrorMessage}")
                    Button(onClick = {
                        scope.launch {
                            horizontalVueReaderState.load(
                                context = context,
                                coroutineScope = scope,
                                containerSize = containerSize,
                                isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT,
                                customResource = null
                            )
                        }
                    }) {
                        Text(text = "Retry")
                    }
                }
            }

            is VueLoadState.DocumentImporting -> {

            }

            is VueLoadState.DocumentLoaded -> {

                HorizontalVueReader(
                    modifier = Modifier.fillMaxSize(),
                    contentModifier = Modifier
                        .padding(PaddingValues(vertical = 10.dp))
                        .fillMaxSize(),
                    horizontalVueReaderState = horizontalVueReaderState
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${horizontalVueReaderState.currentPage} of ${horizontalVueReaderState.pdfPageCount}",
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.38f))
                            .padding(10.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = {
                            horizontalVueReaderState.launchImportIntent(
                                context = context,
                                launcher = launcher
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add Page"
                        )
                    }
                }
                Column(
                    Modifier
                        .align(Alignment.BottomCenter)
                ) {
                    VueHorizontalSlider(
                        modifier = Modifier
                            .padding(horizontal = 10.dp, vertical = 10.dp)
                            .fillMaxWidth()
                            .height(40.dp),
                        horizontalVueReaderState = horizontalVueReaderState
                    )
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            horizontalVueReaderState.rotate(-90F)
                        }) {
                            Icon(imageVector = Icons.Filled.RotateLeft, contentDescription = "")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = {
                            horizontalVueReaderState.rotate(90F)
                        }) {
                            Icon(imageVector = Icons.Filled.RotateRight, contentDescription = "")
                        }
                    }
                }

            }

            is VueLoadState.DocumentLoading -> {
                Column {
                    CircularProgressIndicator()
                    Text(text = "Loading ${horizontalVueReaderState.loadPercent}")
                }
            }
        }
    }
}

@Composable
fun VerticalPdfViewer() {
    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val verticalVueReaderState = rememberVerticalVueReaderState(
        resource = VueResourceType.Remote(
            "https://myreport.altervista.org/Lorem_Ipsum.pdf"
        )
    )
    var containerSize by remember {
        mutableStateOf<IntSize?>(null)
    }
    if (containerSize != null)
        LaunchedEffect(Unit) {
            verticalVueReaderState.load(
                context = context,
                coroutineScope = scope,
                containerSize = containerSize!!,
                isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT,
                customResource = null
            )
        }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged {
                containerSize = it
            },
        contentAlignment = Alignment.Center
    ) {
        when (verticalVueReaderState.vueLoadState) {
            is VueLoadState.DocumentError -> {
                Column {
                    Text(text = "Error:  ${verticalVueReaderState.vueLoadState.getErrorMessage}")
                    Button(onClick = {
                        scope.launch {
                            verticalVueReaderState.load(
                                context = context,
                                coroutineScope = scope,
                                containerSize = containerSize!!,
                                isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT,
                                customResource = null
                            )
                        }
                    }) {
                        Text(text = "Retry")
                    }
                }
            }

            is VueLoadState.DocumentImporting -> {

            }

            is VueLoadState.DocumentLoaded -> {
                VerticalVueReader(
                    modifier = Modifier.fillMaxSize(),
                    contentModifier = Modifier.padding(PaddingValues(vertical = 10.dp)),
                    verticalVueReaderState = verticalVueReaderState,
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${verticalVueReaderState.currentPage} of ${verticalVueReaderState.pdfPageCount}",
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.38f))
                            .padding(10.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = {
                        verticalVueReaderState.rotate(90F)
                    }) {
                        Icon(imageVector = Icons.Filled.Refresh, contentDescription = "")
                    }
                }
            }

            is VueLoadState.DocumentLoading -> {
                Column {
                    CircularProgressIndicator()
                    Text(text = "Loading ${verticalVueReaderState.loadPercent}")
                }
            }
        }
    }
}