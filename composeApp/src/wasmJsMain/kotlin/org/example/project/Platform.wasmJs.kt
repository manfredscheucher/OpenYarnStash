package org.example.project

class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"
    override val fileHandler: FileHandler = WasmJsFileHandler()
}

actual fun getPlatform(context: Any?): Platform = WasmPlatform()
