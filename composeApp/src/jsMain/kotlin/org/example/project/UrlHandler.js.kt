package org.example.project

import kotlinx.browser.window

actual fun openUrl(url: String) {
    window.open(url)
}
