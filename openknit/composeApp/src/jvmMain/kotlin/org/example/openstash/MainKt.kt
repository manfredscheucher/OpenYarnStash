package org.example.openstash

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "OpenStash"
    ) {
        App(JsonRepository(JvmFileHandler()))
    }
}
