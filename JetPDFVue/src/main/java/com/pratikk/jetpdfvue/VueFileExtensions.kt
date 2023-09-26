package com.pratikk.jetpdfvue

import android.content.Context
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

internal fun generateFileName(): String {
    return "${Calendar.getInstance().timeInMillis.getDateddMMyyyyHHmm()}_${Calendar.getInstance().timeInMillis}.pdf"
}

internal fun Long.getDateddMMyyyyHHmm(): String =
    SimpleDateFormat("dd_MM_yyyy_hh_mm", Locale.getDefault()).format(Date(this)).toString()


fun InputStream.toFile(extension:String):File{
    val _file = File.createTempFile("temp",extension)
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