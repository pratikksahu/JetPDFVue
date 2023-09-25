package com.pratikk.jetpdfvue.state

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class VueImportState{

    @Parcelize
    data class Imported(val uri: Uri?): VueImportState(), Parcelable
    @Parcelize
    data class Ideal(private val ideal:Boolean = true) : VueImportState(), Parcelable
}