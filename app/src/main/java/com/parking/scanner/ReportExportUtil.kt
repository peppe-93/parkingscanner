package com.parking.scanner

import android.media.MediaScannerConnection
import android.os.Environment
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun saveSessionReportToDownloads(context: android.content.Context, content: String): File {
    val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    val fileName = "report_${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))}.txt"
    val file = File(downloads, fileName)
    file.writeText(content)
    MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), null, null)
    return file
}