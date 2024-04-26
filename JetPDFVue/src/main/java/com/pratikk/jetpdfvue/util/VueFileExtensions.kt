package com.pratikk.jetpdfvue.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Base64
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import com.pratikk.jetpdfvue.state.VueFileType
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun Uri.getFileType(context: Context): VueFileType {
    val type = context.contentResolver.getType(this)
        ?: scheme ?: throw Throwable("File type cannot be decoded, please check uri $this")
    return if (type.contains("pdf"))
        VueFileType.PDF
    else if (type.contains("text") || type.contains("txt"))
        VueFileType.BASE64
    else if(type == "file") {
        val file = toFile()
        if(file.name.contains("pdf"))
            VueFileType.PDF
        else if (file.name.contains("text") || file.name.contains("txt"))
            VueFileType.BASE64
        else
            VueFileType.IMAGE
    } else
        VueFileType.IMAGE
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
 * Function to recursively compress an image until its size is around threshold
 * Must be called after image has been rotated because exif data would not be retained
 * */

fun File.compressImageToThreshold(threshold: Int) {
    if (exists()) {
        val tempFile = File.createTempFile("tempCompress", ".$extension")
        copyTo(tempFile, true)
        var quality = 100 // Initial quality setting
        var currentSize = tempFile.length()

        while (currentSize > (threshold * 1024 * 1024)) { // 2MB in bytes
            quality -= 5 // Reduce quality in steps of 5
            if (quality < 0) {
                break // Don't reduce quality below 0
            }

            // Compress the image and get its new size
            currentSize = tempFile.compressImage(quality)
        }

        tempFile.copyTo(this,true)
    }
}
/**
 * Extension function to convert any input stream to a file
 * */
fun InputStream.toFile(extension:String):File{
    val _file = File.createTempFile("temp",".${extension}")
    val byteArrayOutputStream = ByteArrayOutputStream()
    val buffer = ByteArray(1024)
    var bytesRead: Int
    while(read(buffer).also { bytesRead = it } != -1){
        byteArrayOutputStream.write(buffer, 0, bytesRead)
    }

    FileOutputStream(_file).use {
        it.write(byteArrayOutputStream.toByteArray())
    }
    return _file
}
/**
 * Get file from Uri
 * */
@Deprecated("Use toFile() to get file from uri")
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
// Function to compress an image and return its size
internal fun File.compressImage(quality: Int): Long {
    try {
        val bitmap = BitmapFactory.decodeFile(absolutePath)

        val outputStream = FileOutputStream(this)
        // Compress the bitmap with the specified quality (0-100)
        if (absolutePath.contains("jpg") || absolutePath.contains("jpeg") || absolutePath.contains("JPG") || absolutePath.contains(
                "JPEG"
            )
        )
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        else
            bitmap.compress(Bitmap.CompressFormat.PNG, quality, outputStream)

        outputStream.flush()
        outputStream.close()

        // Return the size of the compressed image
        return length()
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return 0
}

internal fun generateFileName(): String {
    return "${Calendar.getInstance().timeInMillis.getDateddMMyyyyHHmm()}_${Calendar.getInstance().timeInMillis}.pdf"
}

internal fun Long.getDateddMMyyyyHHmm(): String =
    SimpleDateFormat("dd_MM_yyyy_hh_mm", Locale.getDefault()).format(Date(this)).toString()

internal fun File.toBase64File():File{
    val file = File.createTempFile("temp",".txt")
    val inputStream = FileInputStream(this)
    val byteArrayOutputStream = ByteArrayOutputStream()
    val buffer = ByteArray(1024)
    var bytesRead: Int
    while(inputStream.read(buffer).also { bytesRead = it } != -1){
        byteArrayOutputStream.write(buffer, 0, bytesRead)
    }

    val decodeStream = Base64.decode(byteArrayOutputStream.toByteArray(), Base64.DEFAULT)
    FileOutputStream(file, false).use {
        it.write(decodeStream)
    }
    return file
}