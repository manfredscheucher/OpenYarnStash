package org.example.project

import kotlinx.browser.localStorage
import org.w3c.dom.get
import org.w3c.dom.set

// Use browser's native btoa/atob for base64 encoding
@JsName("btoa")
private external fun btoa(data: String): String

@JsName("atob")
private external fun atob(data: String): String

// Helper to convert between ByteArray and base64 without string corruption
private fun byteArrayToBase64(bytes: ByteArray): String {
    // Use Uint8Array to avoid signed/unsigned issues
    val uint8Array = js("new Uint8Array(bytes.length)").unsafeCast<org.khronos.webgl.Uint8Array>()
    for (i in bytes.indices) {
        uint8Array.asDynamic()[i] = bytes[i].toInt() and 0xFF
    }

    // Convert Uint8Array to binary string for btoa
    val binaryString = js("String.fromCharCode.apply(null, uint8Array)") as String
    return btoa(binaryString)
}

private fun base64ToByteArray(base64: String): ByteArray {
    // Decode base64 to binary string
    val binaryString = atob(base64)

    // Convert binary string to Uint8Array first to avoid UTF-8 issues
    val uint8Array = js("new Uint8Array(binaryString.length)").unsafeCast<org.khronos.webgl.Uint8Array>()
    for (i in 0 until binaryString.length) {
        uint8Array.asDynamic()[i] = binaryString.asDynamic().charCodeAt(i)
    }

    // Convert Uint8Array to ByteArray
    return ByteArray(uint8Array.length) { i ->
        (uint8Array.asDynamic()[i] as Int).toByte()
    }
}

class JsFileHandler : FileHandler {

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
        // Convert ByteArray to base64 without string corruption
        val base64 = byteArrayToBase64(bytes)
        localStorage[path] = "data:image/jpeg;base64,$base64"
    }

    override suspend fun readBytes(path: String): ByteArray? {
        val dataUrl = localStorage[path] ?: return null

        // Check if it's a valid data URL
        if (!dataUrl.contains("base64,")) {
            console.log("Invalid data URL for path $path: missing base64 marker")
            return null
        }

        val base64 = dataUrl.substringAfter("base64,")

        try {
            // Convert base64 to ByteArray without string corruption
            return base64ToByteArray(base64)
        } catch (e: Exception) {
            console.log("Failed to decode base64 for path $path", e)
            return null
        }
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
