package org.example.project

import java.awt.Desktop
import java.net.URI

actual fun sendEmail(address: String, subject: String) {
    val desktop = Desktop.getDesktop()
    val uri = URI("mailto:$address?subject=$subject")
    desktop.mail(uri)
}
