package com.pratikk.jetpdfvue.state

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver

@OptIn(ExperimentalFoundationApi::class)
internal class VuePagerState(
    initialPage: Int,
    initialPageOffsetFraction: Float,
    updatedPageCount: () -> Int
) : PagerState(initialPage, initialPageOffsetFraction) {

    var pageCountState = mutableStateOf(updatedPageCount)
    override val pageCount: Int get() = pageCountState.value.invoke()

    companion object {
        /**
         * To keep current page and current page offset saved
         */
        val Saver: Saver<VuePagerState, *> = listSaver(
            save = {
                listOf(
                    it.currentPage,
                    it.currentPageOffsetFraction,
                    it.pageCount
                )
            },
            restore = {
                VuePagerState(
                    initialPage = it[0] as Int,
                    initialPageOffsetFraction = it[1] as Float,
                    updatedPageCount = { it[2] as Int }
                )
            }
        )
    }
}