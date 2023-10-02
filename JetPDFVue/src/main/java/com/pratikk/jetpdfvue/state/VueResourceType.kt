package com.pratikk.jetpdfvue.state

import android.net.Uri
import android.os.Parcelable
import androidx.annotation.RawRes
import kotlinx.parcelize.Parcelize

/**
 * Enum class to indicate which file is provided with VueResourceType
 * @see VueResourceType
 */
@Parcelize
enum class VueFileType:Parcelable{
    PDF,IMAGE,BASE64
}
sealed class VueResourceType{

    /**
     * @param uri If null then internally an empty file would be create otherwise param uri will be used as file
     * */
    @Parcelize
    data class BlankDocument(val uri:Uri? = null): VueResourceType(), Parcelable

    /**
     * @param uri Source file uri
     * @param fileType Source file type
     * @see VueFileType
     */
    @Parcelize
    data class Local(val uri: Uri,val fileType:VueFileType = VueFileType.PDF) : VueResourceType(), Parcelable

    /**
     * @param url Source file url (Method type GET)
     * @param fileType Source file type
     * @see VueFileType
     * @param headers Headers if required when fetching from url
     */
    @Parcelize
    data class Remote(
        val url: String,
        val headers: HashMap<String,String> = hashMapOf(),
        val fileType:VueFileType
    ) : VueResourceType(), Parcelable

    /**
     * @param assetId Source asset id
     * @param fileType Source file type
     * @see VueFileType
     */
    @Parcelize
    data class Asset(@RawRes val assetId: Int,val fileType:VueFileType) : VueResourceType(), Parcelable

    @Parcelize
    data object Custom : VueResourceType(), Parcelable
}