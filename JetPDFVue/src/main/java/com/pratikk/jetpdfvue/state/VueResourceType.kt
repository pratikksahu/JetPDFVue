package com.pratikk.jetpdfvue.state

import android.net.Uri
import android.os.Parcelable
import androidx.annotation.RawRes
import kotlinx.parcelize.Parcelize

@Parcelize
enum class VueFileType:Parcelable{
    PDF,IMAGE,BASE64
}
sealed class VueResourceType{

    /**
     * @param uri If null then internally an empty file would be create otherwise param uri will be used
     * */
    @Parcelize
    data class BlankDocument(val uri:Uri? = null): VueResourceType(), Parcelable

    @Parcelize
    data class Local(val uri: Uri,val fileType:VueFileType = VueFileType.PDF) : VueResourceType(), Parcelable

    @Parcelize
    data class Remote(
        val url: String,
        val headers: HashMap<String,String> = hashMapOf(),
        val fileType:VueFileType
    ) : VueResourceType(), Parcelable

    @Parcelize
    data class Asset(@RawRes val assetId: Int,val fileType:VueFileType) : VueResourceType(), Parcelable

    @Parcelize
    data object Custom : VueResourceType(), Parcelable
}