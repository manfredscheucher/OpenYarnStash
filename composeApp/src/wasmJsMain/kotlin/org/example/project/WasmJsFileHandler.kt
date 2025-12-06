package org.example.project

@JsModule("js-base64")
external object Base64 {
    fun encode(data: String): String
    fun decode(data: String): String
}

class WasmJsFileHandler : BaseWebFileHandler() {
    override fun encodeBase64(data: String): String = Base64.encode(data)
    override fun decodeBase64(data: String): String = Base64.decode(data)

    override suspend fun zipFiles(): ByteArray {
        // TODO: Implement ZIP export for WasmJS
        // WasmJS has stricter type requirements and doesn't support dynamic/asDynamic the same way as JS
        throw NotImplementedError("ZIP export is not yet implemented for WasmJS target")
    }

    override suspend fun unzipAndReplaceFiles(zipInputStream: Any) {
        // TODO: Implement ZIP import for WasmJS
        // WasmJS has stricter type requirements and doesn't support dynamic/asDynamic the same way as JS
        throw NotImplementedError("ZIP import is not yet implemented for WasmJS target")
    }
}
