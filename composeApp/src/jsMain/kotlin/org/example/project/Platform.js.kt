package org.example.project

class JsPlatform: Platform {
    override val name: String = "Web with Kotlin/JS"
    override val fileHandler: FileHandler = JsFileHandler()
}

actual fun getPlatform(context: Any?): Platform = JsPlatform()