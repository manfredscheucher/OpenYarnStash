package org.example.project

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

actual fun initializeLogger(fileHandler: FileHandler, settings: Settings) {
    Logger.init(fileHandler, settings)
}

fun main() = application {
    val platform = getPlatform()
    val fileHandler = platform.fileHandler
    val jsonDataManager = JsonDataManager(fileHandler, "stash.json")
    val imageManager = ImageManager(fileHandler)
    val settingsManager = JsonSettingsManager(fileHandler, "settings.json")
    val fileDownloader = FileDownloader()
    val backDispatcher = remember { DesktopBackDispatcher() }

    initializeLogger(fileHandler, settingsManager.settings)

    Window(
        onCloseRequest = ::exitApplication,
        title = "OpenYarnStash",
        state = rememberWindowState(width = 1200.dp, height = 800.dp),
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
                jsonDataManager = jsonDataManager,
                imageManager = imageManager,
                fileDownloader = fileDownloader,
                fileHandler = fileHandler,
                settingsManager = settingsManager
            )
        }
    }
}
