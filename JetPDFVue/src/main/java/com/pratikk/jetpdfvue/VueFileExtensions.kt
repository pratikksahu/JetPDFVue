package com.pratikk.jetpdfvue

import android.content.Context
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar
import java.util.Date

internal fun generateFileName(): String {
    return "${Calendar.getInstance().time}_${Calendar.getInstance().timeInMillis}.pdf"
}
internal fun generateTempFileName(fileName:String): String {
    return "${fileName}_${Calendar.getInstance().timeInMillis}.pdf"
}

