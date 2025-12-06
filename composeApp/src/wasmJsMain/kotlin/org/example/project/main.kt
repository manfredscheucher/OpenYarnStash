package org.example.project

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.events.Event

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    window.addEventListener("DOMContentLoaded", {
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
    })
}
