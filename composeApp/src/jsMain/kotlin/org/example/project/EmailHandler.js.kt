package org.example.project

import kotlinx.browser.window

actual fun sendEmail(address: String, subject: String) {
    window.open("mailto:$address?subject=$subject")
}
