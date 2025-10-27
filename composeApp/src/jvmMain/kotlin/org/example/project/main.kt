package org.example.project

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.util.Locale

fun main() = application {
    val fileHandler = JvmFileHandler()
    val jsonDataManager = JsonDataManager(fileHandler, "stash.json")
    val imageManager = ImageManager(fileHandler)
    val settingsManager = JsonSettingsManager(fileHandler, "settings.json")
    Window(onCloseRequest = ::exitApplication) {
        App(jsonDataManager, imageManager, settingsManager)
    }
}

