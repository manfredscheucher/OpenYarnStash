package org.example.openstash

import kotlinx.browser.window

class JsPlatform : Platform {
    override val name: String = "JS ${window.navigator.userAgent}"
}

actual fun getPlatform(): Platform = JsPlatform()