package com.pratikk.jetpdfvue.state

import android.content.Context
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.core.net.toUri
import kotlinx.coroutines.CoroutineScope
import java.io.File

class VerticalVueReaderState(
    resource: VueResourceType
) : VueReaderState(resource) {

    internal var lazyListState = LazyListState(0, 0)
    override suspend fun nextPage(){
        lazyListState.animateScrollToItem(currentPage() + 1)
    }
    override suspend fun prevPage(){
        lazyListState.animateScrollToItem(currentPage() - 1)
    }

    override fun rotate(angle: Float) {
        vueRenderer?.pageLists?.get(currentPage())?.apply {
            rotation += angle % 360F
            refresh()
        }
    }

    override val currentPage: Int
        get() = currentPage() + 1

    private fun currentPage():Int {
        return vueRenderer?.let { pdfRender ->
            val currentMinIndex = lazyListState.firstVisibleItemIndex
            var lastVisibleIndex = currentMinIndex
            var totalVisiblePortion =
                (pdfRender.pageLists[currentMinIndex].dimension.height) - lazyListState.firstVisibleItemScrollOffset
            for (i in currentMinIndex + 1 until pdfPageCount) {
                val newTotalVisiblePortion =
                    totalVisiblePortion + (pdfRender.pageLists[i].dimension.height)
                if (newTotalVisiblePortion <= pdfRender.containerSize.height) {
                    lastVisibleIndex = i
                    totalVisiblePortion = newTotalVisiblePortion
                } else {
                    break
                }
            }
            lastVisibleIndex
        } ?: 0
    }

    override val isScrolling: Boolean
        get() = lazyListState.isScrollInProgress
    override val TAG: String
        get() = "HorizontalVueReader"


    override fun load(
        context: Context,
        coroutineScope: CoroutineScope,
        containerSize: IntSize,
        isPortrait: Boolean,
        customResource: (suspend CoroutineScope.() -> File)?
    ) {
        this.containerSize = containerSize
        this.isPortrait = isPortrait
        loadResource(
            context = context,
            coroutineScope = coroutineScope,
            loadCustomResource = customResource
        )
    }

    companion object {
        val Saver: Saver<VerticalVueReaderState, *> = listSaver(
            save = {
                it.importJob?.cancel()
                val resource = it.file?.let { file ->
                    VueResourceType.Local(
                        file.toUri()
                    )
                } ?: it.vueResource

                buildList {
                    add(resource)
                    add(it.importFile?.absolutePath ?: "NO_IMAGE")
                    add(it.lazyListState.firstVisibleItemIndex)
                    add(it.lazyListState.firstVisibleItemScrollOffset)
                    if (it.vueLoadState is VueLoadState.DocumentImporting)
                        add(it.vueLoadState)
                    else
                        add(VueLoadState.DocumentLoading)
                    add(it.vueImportState)
                }.toList()
            },
            restore = {
                VerticalVueReaderState(it[0] as VueResourceType).apply {
                    //Restore file path
                    importFile = if(it[1] != "NO_IMAGE") File(it[1] as String) else null
                    //Restore list state
                    lazyListState = LazyListState(it[2] as Int, it[3] as Int)
                    //Restoring in case it was in importing state
                    vueLoadState = it[4] as VueLoadState
                    //To resume importing on configuration change
                    vueImportState = it[5] as VueImportState
                }
            }
        )
    }
}

@Composable
fun rememberVerticalVueReaderState(
    resource: VueResourceType,
): VerticalVueReaderState {
    return rememberSaveable(saver = VerticalVueReaderState.Saver) {
        VerticalVueReaderState(resource)
    }
}
