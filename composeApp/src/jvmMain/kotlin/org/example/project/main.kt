package org.example.project

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    val fileHandler = JvmFileHandler()
    val jsonDataManager = JsonDataManager(fileHandler, "stash.json")
    val imageManager = ImageManager(fileHandler)
    val settingsManager = JsonSettingsManager(fileHandler, "settings.json")
    val fileDownloader = FileDownloader()
    Window(onCloseRequest = ::exitApplication) {
        App(jsonDataManager, imageManager, settingsManager, fileDownloader, fileHandler)
    }
}
