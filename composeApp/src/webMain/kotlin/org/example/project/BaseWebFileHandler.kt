package org.example.project

import kotlinx.browser.localStorage
import org.w3c.dom.get
import org.w3c.dom.set

abstract class BaseWebFileHandler : FileHandler {

    override suspend fun readText(path: String): String {
        return localStorage[path] ?: ""
    }

    override suspend fun writeText(path: String, content: String) {
        localStorage[path] = content
    }

    override suspend fun appendText(path: String, content: String) {
        val existingContent = localStorage[path] ?: ""
        localStorage[path] = existingContent + content
    }

    override suspend fun backupFile(path: String): String? {
        val content = localStorage[path]
        if (content != null) {
            val backupKey = createTimestampedFileName(path.substringBeforeLast('.'), path.substringAfterLast('.'))
            localStorage[backupKey] = content
            return backupKey
        }
        return null
    }

    protected abstract fun encodeBase64(data: String): String
    protected abstract fun decodeBase64(data: String): String

    override suspend fun writeBytes(path: String, bytes: ByteArray) {
        val base64 = encodeBase64(bytes.decodeToString())
        localStorage[path] = "data:image/jpeg;base64,$base64"
    }

    override suspend fun readBytes(path: String): ByteArray? {
        val dataUrl = localStorage[path] ?: return null
        val base64 = dataUrl.substringAfter("base64,")
        return decodeBase64(base64).encodeToByteArray()
    }

    override suspend fun deleteFile(path: String) {
        localStorage.removeItem(path)
    }

    abstract override suspend fun zipFiles(): ByteArray

    override suspend fun deleteFilesDirectory() {
        val keysToRemove = mutableListOf<String>()
        for (i in 0 until localStorage.length) {
            localStorage.key(i)?.let { key ->
                if (key.startsWith("files/") || key.startsWith("images/") || key.startsWith("pdf/")) {
                    keysToRemove.add(key)
                }
            }
        }
        keysToRemove.forEach { key ->
            localStorage.removeItem(key)
        }
    }

    override suspend fun renameFilesDirectory(newName: String) {
        throw UnsupportedOperationException("renameFilesDirectory is not supported on JS")
    }

    abstract override suspend fun unzipAndReplaceFiles(zipInputStream: Any)

    override fun openInputStream(path: String): FileInputSource? {
        throw UnsupportedOperationException("openInputStream is not supported on JS")
    }

    override suspend fun listFilesRecursively(path: String): List<String> {
        Logger.log(LogLevel.INFO, "listFilesRecursively: $path")
        val files = mutableListOf<String>()

        for (i in 0 until localStorage.length) {
            val key = localStorage.key(i) ?: continue
            // Filter by path prefix
            if (key.startsWith(path)) {
                files.add(key)
            }
        }

        Logger.log(LogLevel.INFO, "listFilesRecursively found ${files.size} files")
        return files
    }

    override suspend fun getFileHash(path: String): String? {
        return null
    }

    override suspend fun getDirectorySize(path: String): Long {
        Logger.log(LogLevel.INFO, "getDirectorySize: $path")
        var totalSize = 0L

        for (i in 0 until localStorage.length) {
            val key = localStorage.key(i) ?: continue
            if (key.startsWith(path)) {
                val value = localStorage[key] ?: ""
                // Approximate size (UTF-16 encoding in JS, so 2 bytes per char)
                totalSize += (key.length + value.length) * 2
            }
        }

        Logger.log(LogLevel.INFO, "getDirectorySize: $totalSize bytes")
        return totalSize
    }

    override suspend fun getFileSize(path: String): Long {
        val content = localStorage[path] ?: return 0L
        // Approximate size (UTF-16 encoding in JS, so 2 bytes per char)
        return (content.length * 2).toLong()
    }

    override fun openFileExternally(path: String) {
        // Not applicable in browser
    }
}
