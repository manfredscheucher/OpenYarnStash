package org.example.project

import java.awt.Desktop
import java.net.URI

actual fun openUrl(url: String) {
    val desktop = Desktop.getDesktop()
    desktop.browse(URI(url))
}
