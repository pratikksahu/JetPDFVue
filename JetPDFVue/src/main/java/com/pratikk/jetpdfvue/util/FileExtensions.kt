package com.pratikk.jetpdfvue.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/**
 * Get file from Uri
 * */
internal fun Uri.getFile(mContext: Context): File {
    val inputStream = mContext.contentResolver?.openInputStream(this)
    var file: File
    inputStream.use { input ->
        file =
            File(mContext.cacheDir, System.currentTimeMillis().toString() + ".pdf")
        FileOutputStream(file).use { output ->
            val buffer =
                ByteArray(4 * 1024) // or other buffer size
            var read: Int = -1
            while (input?.read(buffer).also {
                    if (it != null) {
                        read = it
                    }
                } != -1) {
                output.write(buffer, 0, read)
            }
            output.flush()
        }
    }
    return file
}
/**
 * Copy Uri to another Uri
 * */
internal fun Uri.copyFile(mContext: Context, pathTo: Uri) {
    mContext.contentResolver?.openInputStream(this).use { inStream ->
        if (inStream == null) return
        mContext.contentResolver?.openOutputStream(pathTo).use { outStream ->
            if (outStream == null) return
            // Transfer bytes from in to out
            val buf = ByteArray(1024)
            var len: Int
            while (inStream.read(buf).also { len = it } > 0) {
                outStream.write(buf, 0, len)
            }
        }
    }
}
/**
 * Set the orientation to portrait
 * Must be called before resizing because after that the exif data would be lost
 * */
fun File.rotateImageIfNeeded(): File {
    var bitmap = BitmapFactory.decodeFile(absolutePath)

    //get exif of the camera while image was taken
    val exifInterface: ExifInterface = ExifInterface(absolutePath)
    val orientation = exifInterface.getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_NORMAL
    )
    val matrix = Matrix()
    var valChanged = false
    if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
        matrix.postRotate(90f)
        valChanged = true
    } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
        matrix.postRotate(180f)
        valChanged = true
    } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
        matrix.postRotate(270f)
        valChanged = true
    }
    if (valChanged) {
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        //save bitmap back to file
        FileOutputStream(absolutePath).use {
            if (absolutePath.contains("jpg") || absolutePath.contains("jpeg") || absolutePath.contains(
                    "JPG"
                ) || absolutePath.contains("JPEG")
            )
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            else
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }
    return this
}

/**
 * Must be called after image has been rotated because exif data would not be retained
 * */
fun File.reduceSize() {
    if (length() < 300000)
        return

    val options = BitmapFactory.Options()
    options.inPreferredConfig = Bitmap.Config.ARGB_8888
    options.inSampleSize = 1
    var bitmap = BitmapFactory.decodeFile(absolutePath, options)

    var width = bitmap.width
    var height = bitmap.height

    val bitmapRatio = width.toFloat() / height.toFloat()
    if (bitmapRatio > 1) {
        width = 1000
        height = (width / bitmapRatio).toInt()
    } else {
        height = 1000
        width = (height * bitmapRatio).toInt()
    }

    bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)

    FileOutputStream(this).use {
        if (absolutePath.contains("jpg") || absolutePath.contains("jpeg") || absolutePath.contains("JPG") || absolutePath.contains(
                "JPEG"
            )
        )
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        else
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
    }
}

fun File.share(context: Context) {
    if (exists()) {
        //call share intent to share file
        val sharingIntent = Intent(Intent.ACTION_SEND)
        val uri = FileProvider.getUriForFile(
            context,
            context.applicationContext.packageName + ".provider",
            this
        )
        if (this.name.contains("pdf"))
            sharingIntent.type = "application/pdf"
        else
            sharingIntent.type = "image/*"
        sharingIntent.putExtra(Intent.EXTRA_STREAM, uri)
        sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(
            Intent.createChooser(
                sharingIntent,
                "Share via"
            )
        )
    }
}