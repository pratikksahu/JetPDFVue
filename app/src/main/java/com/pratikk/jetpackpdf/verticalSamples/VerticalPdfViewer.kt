package com.pratikk.jetpackpdf.verticalSamples

import android.content.res.Configuration
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import com.pratikk.jetpdfvue.state.VerticalVueReaderState
import com.pratikk.jetpdfvue.state.VueLoadState
import com.pratikk.jetpdfvue.util.compressImageToThreshold
import kotlinx.coroutines.launch

@Composable
fun VerticalPdfViewer(verticalVueReaderState: VerticalVueReaderState) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val launcher = verticalVueReaderState.getImportLauncher(interceptResult = {
        it.compressImageToThreshold(2)
    })

    BoxWithConstraints(
        modifier = Modifier,
        contentAlignment = Alignment.Center
    ) {
        val configuration = LocalConfiguration.current
        val containerSize = remember {
            IntSize(constraints.maxWidth, constraints.maxHeight)
        }

        LaunchedEffect(Unit) {
            verticalVueReaderState.load(
                context = context,
                coroutineScope = scope,
                containerSize = containerSize,
                isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT,
                customResource = null
            )
        }
        when (verticalVueReaderState.vueLoadState) {
            is VueLoadState.NoDocument -> {
                Button(onClick = {
                    verticalVueReaderState.launchImportIntent(
                        context = context,
                        launcher = launcher
                    )
                }) {
                    Text(text = "Import Document")
                }
            }

            is VueLoadState.DocumentError -> {
                Column {
                    Text(text = "Error:  ${verticalVueReaderState.vueLoadState.getErrorMessage}")
                    Button(onClick = {
                        scope.launch {
                            verticalVueReaderState.load(
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
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Text(text = "Importing...")
                }
            }

            is VueLoadState.DocumentLoaded -> {
                VerticalSampleA(
                    modifier = Modifier.fillMaxHeight(),
                    verticalVueReaderState = verticalVueReaderState) {
                    verticalVueReaderState.launchImportIntent(
                        context = context,
                        launcher = launcher
                    )
                }

            }

            is VueLoadState.DocumentLoading -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Text(text = "Loading ${verticalVueReaderState.loadPercent}")
                }
            }
        }
    }
}