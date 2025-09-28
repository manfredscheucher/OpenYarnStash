package org.example.openstash

import kotlinx.browser.window

class WasmJsPlatform : Platform {
    override val name: String = "WasmJs ${window.navigator.userAgent}" // Similar to JsPlatform for now
}

actual fun getPlatform(): Platform = WasmJsPlatform()