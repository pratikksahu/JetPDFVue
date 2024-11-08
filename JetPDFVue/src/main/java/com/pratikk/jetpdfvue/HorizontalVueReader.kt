package com.pratikk.jetpdfvue

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.pratikk.jetpdfvue.state.HorizontalVueReaderState
import com.pratikk.jetpdfvue.state.VuePageState
import com.pratikk.jetpdfvue.util.pinchToZoomAndDrag

@Composable
fun HorizontalVueReader(
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    horizontalVueReaderState: HorizontalVueReaderState,
) {
    val vueRenderer = horizontalVueReaderState.vueRenderer
    val currentPage = horizontalVueReaderState.currentPage
    if (horizontalVueReaderState.cache != 0)
        LaunchedEffect(key1 = currentPage, block = {
            vueRenderer?.loadWithCache(currentPage)
        })
    if (vueRenderer != null)
        HorizontalPager(
            modifier = modifier,
            userScrollEnabled = false,
            state = horizontalVueReaderState.pagerState
        ) { idx ->
            val pageContent by vueRenderer.pageLists[idx].stateFlow.collectAsState()
            if (horizontalVueReaderState.cache == 0)
                DisposableEffect(key1 = Unit) {
                    vueRenderer.pageLists[idx].load()
                    onDispose {
                        vueRenderer.pageLists[idx].recycle()
                    }
                }
            AnimatedContent(targetState = pageContent, label = "") {
                when (it) {
                    is VuePageState.BlankState -> {
                        BlankPage(
                            modifier = contentModifier,
                            width = horizontalVueReaderState.containerSize!!.width,
                            height = horizontalVueReaderState.containerSize!!.height
                        )
                    }

                    is VuePageState.LoadedState -> {
                        Image(
                            modifier = contentModifier
                                .clipToBounds()
                                .pinchToZoomAndDrag(),
                            bitmap = it.content.asImageBitmap(),
                            contentDescription = ""
                        )
                    }

                }
            }
        }
}