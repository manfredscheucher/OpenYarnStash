package org.example.project

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

class AndroidFileHandler(private val context: Context) : FileHandler {
    private val file = File(context.filesDir, "stash.json")

    override suspend fun readFile(): String {
        return if (file.exists()) {
            file.readText()
        } else {
            ""
        }
    }

    override suspend fun writeFile(content: String) {
        file.writeText(content)
    }

    override suspend fun backupFile(): String? {
        if (file.exists()) {
            val timestamp = SimpleDateFormat("yyyyMMdd-HHmmss").format(Date())
            val backupFileName = "${file.nameWithoutExtension}-$timestamp.${file.extension}"
            val backupFile = File(context.filesDir, backupFileName)
            file.copyTo(backupFile, overwrite = true)
            return backupFileName
        }
        return null
    }
}
