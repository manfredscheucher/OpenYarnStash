package org.example.project

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport


@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val fileHandler = JsFileHandler()
    val jsonDataManager = JsonDataManager(fileHandler, "stash.json")
    val imageManager = ImageManager(fileHandler)
    val settingsManager = JsonSettingsManager(fileHandler, "settings.json")
    ComposeViewport {
        CompositionLocalProvider(
            LocalFileDownloader provides JsFileDownloader()
        ) {
            App(jsonDataManager, imageManager, settingsManager)
        }
    }
}
