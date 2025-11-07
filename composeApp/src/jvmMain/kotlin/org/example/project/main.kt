package org.example.project

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    val fileHandler = JvmFileHandler()
    val jsonDataManager = JsonDataManager(fileHandler, "stash.json")
    val imageManager = ImageManager(fileHandler)
    val settingsManager = JsonSettingsManager(fileHandler, "settings.json")
    val fileDownloader = FileDownloader()
    val backDispatcher = remember { DesktopBackDispatcher() }

    Window(
        onCloseRequest = ::exitApplication,
        onKeyEvent = {
            if (it.type == KeyEventType.KeyDown && it.key == Key.Escape) {
                backDispatcher.dispatch()
            } else {
                false
            }
        }
    ) {
        CompositionLocalProvider(LocalDesktopBackDispatcher provides backDispatcher) {
            App(
                jsonDataManager,
                imageManager,
                settingsManager,
                fileDownloader,
                fileHandler
            )
        }
    }
}
