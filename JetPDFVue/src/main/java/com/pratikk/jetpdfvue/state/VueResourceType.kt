package com.pratikk.jetpdfvue.state

import android.net.Uri
import android.os.Parcelable
import androidx.annotation.RawRes
import kotlinx.parcelize.Parcelize
import java.io.File

sealed class VueResourceType{
    @Parcelize
    data object BlankDocument: VueResourceType(), Parcelable

    @Parcelize
    data class Local(val uri: Uri) : VueResourceType(), Parcelable

    @Parcelize
    data class Remote(
        val url: String,
        val headers: HashMap<String,String> = hashMapOf()
    ) : VueResourceType(), Parcelable

    @Parcelize
    data class Base64(val file: File) : VueResourceType(), Parcelable

    @Parcelize
    data class RemoteBase64(
        val url: String,
        val headers: HashMap<String,String> = hashMapOf()
    ) : VueResourceType(), Parcelable

    @Parcelize
    data class Asset(@RawRes val assetId: Int) : VueResourceType(), Parcelable

    @Parcelize
    data object Custom : VueResourceType(), Parcelable
}