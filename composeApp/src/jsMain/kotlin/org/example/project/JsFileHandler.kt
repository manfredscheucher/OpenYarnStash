package org.example.project

import kotlinx.browser.localStorage
import kotlinx.coroutines.await
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.w3c.dom.get
import org.w3c.dom.set
import kotlin.js.Promise

@JsModule("js-base64")
@JsNonModule
external object Base64 {
    fun encode(data: String): String
    fun decode(data: String): String
}

@JsModule("jszip")
@JsNonModule
external class JSZip {
    fun file(name: String, data: String)
    fun file(name: String, data: Uint8Array)
    fun file(name: String): dynamic
    fun generateAsync(options: dynamic): Promise<ArrayBuffer>
    fun loadAsync(data: Uint8Array): Promise<JSZip>
    val files: dynamic
}

class JsFileHandler : BaseWebFileHandler() {
    override fun encodeBase64(data: String): String = Base64.encode(data)
    override fun decodeBase64(data: String): String = Base64.decode(data)

    override suspend fun zipFiles(): ByteArray {
        Logger.log(LogLevel.INFO, "zipFiles started for JS target")
        val zip = JSZip()

        // Collect all localStorage items
        for (i in 0 until localStorage.length) {
            val key = localStorage.key(i) ?: continue
            val value = localStorage[key] ?: continue

            // Add file to ZIP
            zip.file(key, value)
        }

        // Generate ZIP as ArrayBuffer
        val options = js("({type: 'arraybuffer'})")
        val arrayBuffer = zip.generateAsync(options).await()

        // Convert ArrayBuffer to ByteArray
        val uint8Array = Uint8Array(arrayBuffer)
        val byteArray = ByteArray(uint8Array.length) { i ->
            uint8Array.asDynamic()[i].unsafeCast<Byte>()
        }

        Logger.log(LogLevel.INFO, "zipFiles complete, size: ${byteArray.size}")
        return byteArray
    }

    override suspend fun unzipAndReplaceFiles(zipInputStream: Any) {
        Logger.log(LogLevel.INFO, "unzipAndReplaceFiles started for JS target")

        // zipInputStream should be ByteArray
        val byteArray = zipInputStream as? ByteArray
            ?: throw IllegalArgumentException("Expected ByteArray for zipInputStream")

        // Convert ByteArray to Uint8Array
        val uint8Array = Uint8Array(byteArray.size)
        byteArray.forEachIndexed { index, byte ->
            uint8Array.asDynamic()[index] = byte
        }

        // Load ZIP
        val zip = JSZip().loadAsync(uint8Array).await()

        // Clear existing data
        deleteFilesDirectory()

        // Extract all files
        val files = zip.files
        val fileNames = js("Object.keys(files)").unsafeCast<Array<String>>()

        for (fileName in fileNames) {
            val file = files[fileName]
            // Check if it's a file (not a directory)
            if (file.dir != true) {
                val zipObject = zip.file(fileName)
                if (zipObject != null) {
                    val content = zipObject.asDynamic().async("string").await()
                    localStorage[fileName] = content.unsafeCast<String>()
                }
            }
        }

        Logger.log(LogLevel.INFO, "unzipAndReplaceFiles complete")
    }
}
