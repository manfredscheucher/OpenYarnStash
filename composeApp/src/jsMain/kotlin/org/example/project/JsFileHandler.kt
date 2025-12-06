package org.example.project

@JsModule("js-base64")
@JsNonModule
external object Base64 {
    fun encode(data: String): String
    fun decode(data: String): String
}

class JsFileHandler : BaseWebFileHandler() {
    override fun encodeBase64(data: String): String = Base64.encode(data)
    override fun decodeBase64(data: String): String = Base64.decode(data)
}
