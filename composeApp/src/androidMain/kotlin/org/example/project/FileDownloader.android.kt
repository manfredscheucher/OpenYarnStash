package org.example.project

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

actual class FileDownloader {

    actual fun download(fileName: String, data: ByteArray, context: Any?) {
        val androidContext = context as? Context
        if (androidContext == null) {
            GlobalScope.launch { Logger.log(LogLevel.ERROR, "FileDownloader: Context is not an Android Context") }
            return
        }

        try {
            val file = File(androidContext.filesDir, fileName)
            file.writeBytes(data)
            val uri = FileProvider.getUriForFile(androidContext, "${androidContext.packageName}.provider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/zip"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(intent, "Export ZIP")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            androidContext.startActivity(chooser)
            GlobalScope.launch { Logger.log(LogLevel.DEBUG, "FileDownloader: Successfully created intent for ZIP file.") }
        } catch (e: Exception) {
            GlobalScope.launch { Logger.log(LogLevel.ERROR, "FileDownloader: Error while downloading ZIP file: ${e.message}", e) }
        }
    }
}
