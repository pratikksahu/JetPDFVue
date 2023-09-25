package com.pratikk.jetpdfvue.state

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class VueLoadState{
    val getErrorMessage
        get() = if(this is DocumentError) error?.message.toString() else null
    @Parcelize
    object DocumentLoading : VueLoadState(),Parcelable
    @Parcelize
    object DocumentImporting : VueLoadState(),Parcelable
    @Parcelize
    object DocumentLoaded : VueLoadState(),Parcelable
    @Parcelize
    data class DocumentError(val error: Throwable?) : VueLoadState(),Parcelable
}