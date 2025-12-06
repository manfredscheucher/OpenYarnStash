package org.example.project

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val platform = getPlatform()
    val fileHandler = platform.fileHandler
    val jsonDataManager = JsonDataManager(fileHandler, "stash.json")
    val imageManager = ImageManager(fileHandler)
    val settingsManager = JsonSettingsManager(fileHandler, "settings.json")
    val fileDownloader = FileDownloader()

    ComposeViewport(document.body!!) {
        App(
            jsonDataManager = jsonDataManager,
            imageManager = imageManager,
            fileDownloader = fileDownloader,
            fileHandler = fileHandler,
            settingsManager = settingsManager
        )
    }
}
