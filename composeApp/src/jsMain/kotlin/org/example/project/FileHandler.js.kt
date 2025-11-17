package org.example.project

import kotlinx.browser.localStorage
import kotlinx.coroutines.await
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.w3c.files.File

@JsModule("js-base64")
@JsNonModule
external object Base64 {
    fun encode(data: String): String
    fun decode(data: String): String
}

actual class JsFileHandler : FileHandler {

    override suspend fun readText(path: String): String {
        return localStorage.getItem(path) ?: ""
    }

    override suspend fun writeText(path: String, content: String) {
        localStorage.setItem(path, content)
    }

    override suspend fun backupFile(path: String): String? {
        val content = localStorage.getItem(path)
        if (content != null) {
            val backupKey = createTimestampedFileName(path.substringBeforeLast('.'), path.substringAfterLast('.'))
            localStorage.setItem(backupKey, content)
            return backupKey
        }
        return null
    }

    override fun createTimestampedFileName(baseName: String, extension: String): String {
        val now = Clock.System.now()
        val zone = TimeZone.currentSystemDefault()
        val local = now.toLocalDateTime(zone)
        // YYYYMMDD-HHMMSS
        val timestamp = buildString {
            append(local.year.toString().padStart(4, '0'))
            append(local.monthNumber.toString().padStart(2, '0'))
            append(local.dayOfMonth.toString().padStart(2, '0'))
            append('-')
            append(local.hour.toString().padStart(2, '0'))
            append(local.minute.toString().padStart(2, '0'))
            append(local.second.toString().padStart(2, '0'))
        }
        return "$baseName-$timestamp.$extension"
    }

    override suspend fun writeBytes(path: String, bytes: ByteArray) {
        val base64 = Base64.encode(bytes.decodeToString())
        localStorage.setItem(path, "data:image/jpeg;base64,$base64")
    }

    override suspend fun readBytes(path: String): ByteArray? {
        val dataUrl = localStorage.getItem(path) ?: return null
        val base64 = dataUrl.substringAfter("base64,")
        return Base64.decode(base64).encodeToByteArray()
    }

    override suspend fun deleteFile(path: String) {
        localStorage.removeItem(path)
    }

    override suspend fun zipFiles(): ByteArray {
        throw NotImplementedError("ZIP export is not supported for JS target.")
    }

    override suspend fun zipFiles(outputStream: OutputStream) {
        throw NotImplementedError("ZIP export is not supported for JS target.")
    }
}
