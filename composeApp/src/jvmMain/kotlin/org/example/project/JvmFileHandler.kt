package org.example.project

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

class JvmFileHandler : FileHandler {
    private val file = File("stash.json")

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
            val backupFile = File(backupFileName)
            file.copyTo(backupFile, overwrite = true)
            return backupFileName
        }
        return null
    }

    override suspend fun writeBytes(path: String, bytes: ByteArray) {
        val imageFile = File(path)
        imageFile.parentFile?.mkdirs()
        imageFile.writeBytes(bytes)
    }

    override suspend fun readBytes(path: String): ByteArray? {
        val imageFile = File(path)
        return if (imageFile.exists()) {
            imageFile.readBytes()
        } else {
            null
        }
    }

    override suspend fun deleteFile(path: String) {
        val fileToDelete = File(path)
        if (fileToDelete.exists()) {
            fileToDelete.delete()
        }
    }
}
