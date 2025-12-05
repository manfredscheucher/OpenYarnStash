package org.example.project

import kotlinx.browser.localStorage
import org.w3c.dom.get
import org.w3c.dom.set

@JsModule("js-base64")
external object Base64 {
    fun encode(data: String): String
    fun decode(data: String): String
}

class WasmJsFileHandler : FileHandler {

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

    override fun createTimestampedFileName(baseName: String, extension: String): String {
        return "$baseName-${getCurrentTimestamp()}.$extension"
    }

    override suspend fun writeBytes(path: String, bytes: ByteArray) {
        val base64 = Base64.encode(bytes.decodeToString())
        localStorage[path] = "data:image/jpeg;base64,$base64"
    }

    override suspend fun readBytes(path: String): ByteArray? {
        val dataUrl = localStorage[path] ?: return null
        val base64 = dataUrl.substringAfter("base64,")
        return Base64.decode(base64).encodeToByteArray()
    }

    override suspend fun deleteFile(path: String) {
        localStorage.removeItem(path)
    }

    override suspend fun zipFiles(): ByteArray {
        throw NotImplementedError("ZIP export is not supported for JS target.")
    }

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

    override suspend fun unzipAndReplaceFiles(zipInputStream: Any) {
        throw UnsupportedOperationException("unzipAndReplaceFiles is not supported on JS")
    }

    override fun openInputStream(path: String): FileInputSource? {
        throw UnsupportedOperationException("openInputStream is not supported on JS")
    }

    override suspend fun listFilesRecursively(path: String): List<String> {
        return emptyList() // Not really supported in browser
    }

    override suspend fun getFileHash(path: String): String? {
        return null
    }

    override suspend fun getDirectorySize(path: String): Long {
        return 0L
    }

    override suspend fun getFileSize(path: String): Long {
        return (localStorage[path]?.length ?: 0).toLong()
    }

    override fun openFileExternally(path: String) {
        // Not applicable in browser
    }
}
