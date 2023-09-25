package com.pratikk.jetpdfvue.state

import android.graphics.Bitmap

sealed interface VuePageState{
    data class LoadedState(
        val content: Bitmap
    ) : VuePageState

    data class BlankState(
        val width: Int,
        val height: Int
    ) : VuePageState
}