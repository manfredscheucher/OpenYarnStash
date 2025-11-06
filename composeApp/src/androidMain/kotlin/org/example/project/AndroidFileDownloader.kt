package org.example.project

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

actual class FileDownloader(private val context: Context) {
    actual fun download(fileName: String, data: String) {
        val file = File(context.cacheDir, fileName)
        file.writeText(data)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Export JSON"))
    }

    actual fun download(fileName: String, data: ByteArray) {
        val file = File(context.cacheDir, fileName)
        file.writeBytes(data)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Export ZIP"))
    }
}
