package org.example.project

import kotlinx.browser.localStorage
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag

@JsModule("is-base64")
@JsNonModule
external fun isBase64(str: String): Boolean

@JsModule("js-base64")
@JsNonModule
external object Base64 {
    fun encode(data: String): String
    fun decode(data: String): String
}

class JsFileHandler : FileHandler {
    private val key = "stash.json"

    override suspend fun readFile(): String {
        return localStorage.getItem(key) ?: ""
    }

    override suspend fun writeFile(content: String) {
        localStorage.setItem(key, content)
    }

    override suspend fun backupFile(): String? {
        val content = localStorage.getItem(key)
        if (content != null) {
            val timestamp = getCurrentTimestamp()
            val backupKey = "${key.substringBeforeLast('.')}-$timestamp.${key.substringAfterLast('.')}"
            localStorage.setItem(backupKey, content)
            return backupKey
        }
        return null
    }

    override suspend fun writeBytes(path: String, bytes: ByteArray) {
        val base64 = Base64.encode(bytes.decodeToString())
        localStorage.setItem(path, "data:image/jpeg;base64,$base64")
    }
}
