package org.example.project

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

class JvmFileHandler : FileHandler {

    private val baseDir: File

    init {
        val home = System.getProperty("user.home")
        baseDir = File(home, ".openyarnstash")
        if (!baseDir.exists()) {
            baseDir.mkdirs()
        }
    }

    private fun getFile(path: String) = File(baseDir, path)

    override suspend fun readFile(path: String): String {
        val file = getFile(path)
        return if (file.exists()) {
            file.readText()
        } else {
            ""
        }
    }

    override suspend fun writeFile(path: String, content: String) {
        val file = getFile(path)
        file.parentFile?.mkdirs()
        file.writeText(content)
    }

    override suspend fun backupFile(path: String): String? {
        val file = getFile(path)
        if (file.exists()) {
            val backupFileName = createTimestampedFileName(file.nameWithoutExtension, file.extension)
            val backupFile = File(file.parent, backupFileName)
            file.copyTo(backupFile, overwrite = true)
            return backupFile.name
        }
        return null
    }

    override fun createTimestampedFileName(baseName: String, extension: String): String {
        val timestamp = SimpleDateFormat("yyyyMMdd-HHmmss").format(Date())
        return "$baseName-$timestamp.$extension"
    }

    override suspend fun writeBytes(path: String, bytes: ByteArray) {
        val imageFile = getFile(path)
        imageFile.parentFile?.mkdirs()
        imageFile.writeBytes(bytes)
    }

    override suspend fun readBytes(path: String): ByteArray? {
        val imageFile = getFile(path)
        return if (imageFile.exists()) {
            imageFile.readBytes()
        } else {
            null
        }
    }

    override suspend fun deleteFile(path: String) {
        val fileToDelete = getFile(path)
        if (fileToDelete.exists()) {
            fileToDelete.delete()
        }
    }
}
