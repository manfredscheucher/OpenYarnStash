package org.example.project

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

    var backHandler: (() -> Unit)? by remember { mutableStateOf(null) }
    var backHandlingEnabled by remember { mutableStateOf(false) }

    val setDesktopBackHandler = fun(enabled: Boolean, onBack: () -> Unit) {
        backHandlingEnabled = enabled
        backHandler = onBack
    }

    Window(
        onCloseRequest = ::exitApplication,
        onKeyEvent = {
            if (backHandlingEnabled && it.type == KeyEventType.KeyDown && it.key == Key.Escape) {
                backHandler?.invoke()
                true
            } else {
                false
            }
        }
    ) {
        App(
            jsonDataManager,
            imageManager,
            settingsManager,
            fileDownloader,
            fileHandler,
            setDesktopBackHandler
        )
    }
}
