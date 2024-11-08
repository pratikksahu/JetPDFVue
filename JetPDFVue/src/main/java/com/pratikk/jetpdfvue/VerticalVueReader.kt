package com.pratikk.jetpdfvue

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.VerticalPager
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
import com.pratikk.jetpdfvue.state.VerticalVueReaderState
import com.pratikk.jetpdfvue.state.VuePageState
import com.pratikk.jetpdfvue.util.pinchToZoomAndDrag

@Composable
fun VerticalVueReader(
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    verticalVueReaderState: VerticalVueReaderState
) {
    val vueRenderer = verticalVueReaderState.vueRenderer
    val currentPage = verticalVueReaderState.currentPage
    if (verticalVueReaderState.cache != 0)
        LaunchedEffect(key1 = currentPage, block = {
            vueRenderer?.loadWithCache(currentPage)
        })
    if (vueRenderer != null)
        VerticalPager(
            modifier = modifier,
            userScrollEnabled = false,
            state = verticalVueReaderState.pagerState
        ) { idx ->
            val pageContent by vueRenderer.pageLists[idx].stateFlow.collectAsState()
            if (verticalVueReaderState.cache == 0)
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
                            width = verticalVueReaderState.containerSize!!.width,
                            height = verticalVueReaderState.containerSize!!.height
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