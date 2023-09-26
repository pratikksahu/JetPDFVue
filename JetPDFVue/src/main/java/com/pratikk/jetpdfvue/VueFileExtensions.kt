package com.pratikk.jetpdfvue

import android.content.Context
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

internal fun generateFileName(): String {
    return "${Calendar.getInstance().timeInMillis.getDateddMMyyyyHHmm()}_${Calendar.getInstance().timeInMillis}.pdf"
}

internal fun Long.getDateddMMyyyyHHmm(): String =
    SimpleDateFormat("dd_MM_yyyy_hh_mm", Locale.getDefault()).format(Date(this)).toString()