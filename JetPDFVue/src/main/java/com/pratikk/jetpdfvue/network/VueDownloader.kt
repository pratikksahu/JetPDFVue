package com.pratikk.jetpdfvue.network

import android.content.Context
import android.util.Base64
import com.pratikk.jetpdfvue.state.VueResourceType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.UnknownHostException

suspend fun CoroutineScope.VueDownload(
    vueResource: VueResourceType,
    file: File,
    onProgressChange:(Int) -> Unit,
    onSuccess: () -> Unit,
    onError: (Exception) -> Unit
) {
    withContext(Dispatchers.IO){
        try {
            // URL of the file you want to download
            val fileUrl = when (vueResource) {
                is VueResourceType.Remote -> vueResource.url
                is VueResourceType.RemoteBase64 -> vueResource.url
                else -> throw Exception("Invalid resource type")
            }

            // Create a URL object
            val url = URL(fileUrl)

            // Open a connection to the URL
            val connection = url.openConnection() as HttpURLConnection

            // Set request method (GET is used for downloading files)
            connection.requestMethod = "GET"

            // Set custom headers if needed
            when (vueResource) {
                is VueResourceType.Remote -> {
                    vueResource.headers.forEach {
                        connection.setRequestProperty(it.key, it.value)
                    }
                }

                is VueResourceType.RemoteBase64 -> {
                    vueResource.headers.forEach {
                        connection.setRequestProperty(it.key, it.value)
                    }
                }

                else -> {}
            }

            // Get the input stream from the connection
            val inputStream = connection.inputStream

            // Create a BufferedInputStream for efficient reading
            val bufferedInputStream = BufferedInputStream(inputStream)

            //Write to file when resource type is pdf
            val outputStream = FileOutputStream(file.absolutePath)

            //Write to temporary byte array when resource type is base64
            val byteArrayOutputStream = ByteArrayOutputStream()

            // Get the content length (file size) from the response headers
            val contentLength = connection.getHeaderField("Content-Length")
            val fileSize = contentLength?.toLong() ?: throw Exception("File not found on server")
            // Read and write the file data
            val buffer = ByteArray(1024)
            var bytesRead: Int
            var totalBytesRead = 0
            when (vueResource) {
                is VueResourceType.Remote -> {
                    while (bufferedInputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead
                        onProgressChange((totalBytesRead.toDouble() / fileSize.toDouble() * 100).toInt())
                    }
                    // Close the streams
                    outputStream.close()
                    bufferedInputStream.close()
                    onSuccess()
                }

                is VueResourceType.RemoteBase64 -> {
                    while (bufferedInputStream.read(buffer).also { bytesRead = it } != -1) {
                        byteArrayOutputStream.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead
                        onProgressChange((totalBytesRead.toDouble() / fileSize.toDouble() * 100).toInt())
                    }

                    val decodedByteStream = Base64.decode(byteArrayOutputStream.toByteArray(),Base64.DEFAULT)
                    outputStream.write(decodedByteStream)

                    // Close the streams
                    outputStream.close()
                    bufferedInputStream.close()
                    onSuccess()
                }

                else -> {}
            }
        }catch (e: FileNotFoundException) {
            file.delete()
            onError(Exception("File not found"))
        } catch (e: UnknownHostException) {
            file.delete()
            onError(Exception("No internet connection"))
        }  catch (e: Exception) {
            onError(e)
        }
    }
}
