package com.pratikk.jetpdfvue.state

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.unit.IntSize
import androidx.core.net.toUri
import kotlinx.coroutines.CoroutineScope
import java.io.File

class HorizontalVueReaderState(
    resource: VueResourceType
) : VueReaderState(resource) {
    internal var pagerState = VuePagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0f,
        updatedPageCount = { pdfPageCount })

    override suspend fun nextPage() {
        pagerState.animateScrollToPage(pagerState.currentPage + 1)
    }

    override suspend fun prevPage() {
        pagerState.animateScrollToPage(pagerState.currentPage - 1)
    }

    override fun rotate(angle: Float) {
        vueRenderer?.pageLists?.get(pagerState.currentPage)?.apply {
            rotation += angle % 360F
            refresh()
        }
    }

    override val currentPage: Int
        get() = pagerState.currentPage + 1
    override val isScrolling: Boolean
        get() = pagerState.isScrollInProgress
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
        val Saver: Saver<HorizontalVueReaderState, *> = listSaver(
            save = {
                it.importJob?.cancel()
                val resource =
                    it.file?.let { file ->
                        if (it.vueResource is VueResourceType.BlankDocument && !it.isDocumentModified)
                            VueResourceType.BlankDocument(file.toUri())
                        else
                            VueResourceType.Local(
                                file.toUri(),
                                it.getResourceType()
                            )
                    } ?: it.vueResource

                buildList {
                    add(resource)
                    add(it.importFile?.absolutePath)
                    add(it.pagerState.currentPage)
                    if (it.vueLoadState is VueLoadState.DocumentImporting)
                        add(it.vueLoadState)
                    else
                        add(VueLoadState.DocumentLoading)
                    add(it.vueImportState)
                    add(it.mDocumentModified)
                    add(it.cache)
                }.toList()
            },
            restore = {
                HorizontalVueReaderState(it[0] as VueResourceType).apply {
                    //Restore file path
                    importFile = if (it[1] != null) File(it[1] as String) else null
                    //Restore Pager State
                    pagerState = VuePagerState(
                        initialPage = it[2] as Int,
                        initialPageOffsetFraction = 0F,
                        updatedPageCount = { pdfPageCount })
                    //Restoring in case it was in importing state
                    vueLoadState = it[3] as VueLoadState
                    //To resume importing on configuration change
                    vueImportState = it[4] as VueImportState
                    //Restore document modified flag
                    mDocumentModified = it[5] as Boolean
                    //Restore cache value
                    cache = it[6] as Int
                }
            }
        )
    }
}

@Composable
fun rememberHorizontalVueReaderState(
    resource: VueResourceType,
    cache: Int = 0
): HorizontalVueReaderState {
    return rememberSaveable(saver = HorizontalVueReaderState.Saver) {
        HorizontalVueReaderState(resource).apply { this.cache = cache }
    }
}
