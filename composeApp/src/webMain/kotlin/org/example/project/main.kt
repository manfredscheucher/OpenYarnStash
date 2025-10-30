package org.example.project

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val fileHandler = JsFileHandler()
    val jsonDataManager = JsonDataManager(fileHandler)
    val imageManager = ImageManager(fileHandler)
    val settingsManager = JsonSettingsManager(fileHandler)
    val fileDownloader = JsFileDownloader()

    ComposeViewport("root") {
        App(jsonDataManager, imageManager, settingsManager, fileDownloader, fileHandler)
    }
}
