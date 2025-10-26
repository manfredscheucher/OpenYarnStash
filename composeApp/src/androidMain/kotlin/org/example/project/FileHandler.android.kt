package org.example.project

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

class AndroidFileHandler(private val context: Context) : FileHandler {

    override suspend fun readFile(path: String): String {
        val file = File(context.filesDir, path)
        return if (file.exists()) {
            file.readText()
        } else {
            ""
        }
    }

    override suspend fun writeFile(path: String, content: String) {
        val file = File(context.filesDir, path)
        file.writeText(content)
    }

    override suspend fun backupFile(path: String): String? {
        val file = File(context.filesDir, path)
        if (file.exists()) {
            val timestamp = SimpleDateFormat("yyyyMMdd-HHmmss").format(Date())
            val backupFileName = "${file.nameWithoutExtension}-$timestamp.${file.extension}"
            val backupFile = File(context.filesDir, backupFileName)
            file.copyTo(backupFile, overwrite = true)
            return backupFileName
        }
        return null
    }

    override suspend fun writeBytes(path: String, bytes: ByteArray) {
        val imageFile = File(context.filesDir, path)
        imageFile.parentFile?.mkdirs()
        imageFile.writeBytes(bytes)
    }

    override suspend fun readBytes(path: String): ByteArray? {
        val imageFile = File(context.filesDir, path)
        return if (imageFile.exists()) {
            imageFile.readBytes()
        } else {
            null
        }
    }

    override suspend fun deleteFile(path: String) {
        val fileToDelete = File(context.filesDir, path)
        if (fileToDelete.exists()) {
            fileToDelete.delete()
        }
    }
}
