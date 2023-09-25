package com.pratikk.jetpdfvue.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.io.File
import java.io.FileOutputStream
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Util function to add page to pdf
 * page can be a file or a bitmap
 * */
suspend fun addImageToPdf(
    imageFilePath: String? = null,
    bitmap: Bitmap? = null,
    pdfPath: String
) {
    withContext(Dispatchers.IO) {
        if (imageFilePath.isNullOrEmpty() && bitmap == null)
            throw Exception("Image file or bitmap required")
        val pdfFile = File.createTempFile("temp", ".pdf")
        File(pdfPath).copyTo(pdfFile, true)
        val pdfDocument = PdfDocument()
        val options = BitmapFactory.Options()
        val image = if (!imageFilePath.isNullOrEmpty()) BitmapFactory.decodeFile(
            imageFilePath,
            options
        ) else bitmap!!

        if (!pdfFile.exists()) {
            if (pdfFile.parentFile?.exists() == false)
                pdfFile.parentFile?.mkdirs()
            pdfFile.createNewFile()
            val pageInfo = PdfDocument.PageInfo.Builder(image.width, image.height, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            canvas.drawBitmap(image, 0f, 0f, null)
            pdfDocument.finishPage(page)
            // Save the changes to the existing or new PDF document
            val outputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            return@withContext
        }
        val mrenderer = PdfRenderer(
            ParcelFileDescriptor.open(
                pdfFile,
                ParcelFileDescriptor.MODE_READ_ONLY
            )
        )

        for (i in 0 until mrenderer.pageCount) {
            val originalPage = mrenderer.openPage(i)

            // Create a new bitmap to draw the contents of the original page onto
            val pageBitmap = Bitmap.createBitmap(
                originalPage.width,
                originalPage.height,
                Bitmap.Config.ARGB_8888
            )
            // Draw the contents of the original page onto the pageBitmap
            originalPage.render(pageBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            // Close the original page
            originalPage.close()

            //Create new page for pageBitmap
            val pageInfo =
                PdfDocument.PageInfo.Builder(pageBitmap.width, pageBitmap.height, i + 1)
                    .create()
            val currentPage = pdfDocument.startPage(pageInfo)

            val mCanvas = currentPage.canvas


            // Draw the pageBitmap onto the canvas of the existing page
            mCanvas.drawBitmap(pageBitmap, 0f, 0f, null)
            pageBitmap.recycle()
            pdfDocument.finishPage(currentPage)
            yield()
        }

        // Create a new page in the existing
        val pageCount = mrenderer.pageCount
        val newPage = pdfDocument.startPage(
            PdfDocument.PageInfo.Builder(
                image.width,
                image.height,
                pageCount + 1
            ).create()
        )
        val canvas = newPage.canvas
        // Draw the image on the canvas of the new page
        canvas.drawBitmap(image, 0f, 0f, null)

        // Finish the new page
        pdfDocument.finishPage(newPage)

        // Save the changes to the existing or new PDF document
        val outputStream = FileOutputStream(pdfFile)
        pdfDocument.writeTo(outputStream)
        if (isActive) {
            pdfFile.copyTo(File(pdfPath), true)
        }
        // Close the PDF document and PDF renderer
        pdfDocument.close()
        mrenderer.close()
        image.recycle()
    }
}
/**
 * Util function to merge two pdf
 * imported pdf pages will get appended to old pdf
 * */
suspend fun mergePdf(oldPdfPath: String, importedPdfPath: String) {
    withContext(Dispatchers.IO) {
        val tempOldPdf = File.createTempFile("temp_old", ".pdf")
        val importedPdf = File(importedPdfPath)
        File(oldPdfPath).copyTo(tempOldPdf, true)
        val pdfDocument = PdfDocument()
        var pdfDocumentPage = 1

        val oldRenderer = PdfRenderer(
            ParcelFileDescriptor.open(
                tempOldPdf,
                ParcelFileDescriptor.MODE_READ_ONLY
            )
        )
        val newRenderer = PdfRenderer(
            ParcelFileDescriptor.open(
                importedPdf,
                ParcelFileDescriptor.MODE_READ_ONLY
            )
        )

        //Load old pdf pages
        for (i in 0 until oldRenderer.pageCount) {
            val originalPage = oldRenderer.openPage(i)

            // Create a new bitmap to draw the contents of the original page onto
            val bitmap = Bitmap.createBitmap(
                originalPage.width,
                originalPage.height,
                Bitmap.Config.ARGB_8888
            )
            // Draw the contents of the original page onto the bitmap
            originalPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            // Close the original page
            originalPage.close()

            //Create new page for bitmap
            val pageInfo =
                PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, pdfDocumentPage)
                    .create()
            val currentPage = pdfDocument.startPage(pageInfo)

            val mCanvas = currentPage.canvas


            // Draw the bitmap onto the canvas of the existing page
            mCanvas.drawBitmap(bitmap, 0f, 0f, null)
            bitmap.recycle()
            pdfDocument.finishPage(currentPage)
            pdfDocumentPage += 1
            yield()
        }
        //Load new pdf pages
        for (i in 0 until newRenderer.pageCount) {
            val originalPage = newRenderer.openPage(i)

            // Create a new bitmap to draw the contents of the original page onto
            val bitmap = Bitmap.createBitmap(
                originalPage.width,
                originalPage.height,
                Bitmap.Config.ARGB_8888
            )
            // Draw the contents of the original page onto the bitmap
            originalPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            // Close the original page
            originalPage.close()

            //Create new page for bitmap
            val pageInfo =
                PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, pdfDocumentPage)
                    .create()
            val currentPage = pdfDocument.startPage(pageInfo)

            val mCanvas = currentPage.canvas


            // Draw the bitmap onto the canvas of the existing page
            mCanvas.drawBitmap(bitmap, 0f, 0f, null)
            bitmap.recycle()
            pdfDocument.finishPage(currentPage)
            pdfDocumentPage += 1
            yield()
        }
        val outputStream = FileOutputStream(tempOldPdf)
        pdfDocument.writeTo(outputStream)
        if (isActive) {
            tempOldPdf.copyTo(File(oldPdfPath), true)
        }
        // Close the PDF document and PDF renderer
        pdfDocument.close()
        oldRenderer.close()
        newRenderer.close()
    }
}

@OptIn(ExperimentalFoundationApi::class)
fun Modifier.pinchToZoomAndDrag() = composed {
    val angle by remember { mutableStateOf(0f) }
    var zoom by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    val PI = 3.14
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp.value * 1.2f
    val screenHeight = configuration.screenHeightDp.dp.value * 1.2f
    DisposableEffect(key1 = Unit, effect ={
        onDispose {
            zoom = 1f
            offsetX = 0f
            offsetY = 0f
        }
    })
    combinedClickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = {},
        onDoubleClick = {
            zoom = if (zoom > 1f) 1f
            else 3f
        }
    )
        .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
        .graphicsLayer(
            scaleX = zoom,
            scaleY = zoom,
            rotationZ = angle,
        )
        .pointerInput(Unit) {
            detectTransformGestures(
                onGesture = { _, pan, gestureZoom, _ ->
                    zoom = (zoom * gestureZoom).coerceIn(1F..4F)
                    if (zoom > 1) {
                        val x = (pan.x * zoom)
                        val y = (pan.y * zoom)
                        val angleRad = angle * PI / 180.0

                        offsetX =
                            (offsetX + (x * cos(angleRad) - y * sin(angleRad)).toFloat()).coerceIn(
                                -(screenWidth * zoom)..(screenWidth * zoom)
                            )
                        offsetY =
                            (offsetY + (x * sin(angleRad) + y * cos(angleRad)).toFloat()).coerceIn(
                                -(screenHeight * zoom)..(screenHeight * zoom)
                            )
                    } else {
                        offsetX = 0F
                        offsetY = 0F
                    }
                }
            )
        }
}