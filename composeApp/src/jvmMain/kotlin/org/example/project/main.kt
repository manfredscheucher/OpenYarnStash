package org.example.project

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "OpenYarnStash",
    ) {
        CompositionLocalProvider(
            LocalFileDownloader provides JvmFileDownloader()
        ) {
            App(JsonRepository(JvmFileHandler()))
        }
    }
}