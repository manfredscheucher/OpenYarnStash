package org.example.project

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    print("na sowas, wie komm ich denn hier her?!")
    Window(
        onCloseRequest = ::exitApplication,
        title = "OpenYarnStash",
    ) {
        App()
    }
}