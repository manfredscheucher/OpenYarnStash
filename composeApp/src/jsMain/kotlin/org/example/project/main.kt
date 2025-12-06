package org.example.project

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val platform = getPlatform()
    val fileHandler = platform.fileHandler
    val settingsManager = JsonSettingsManager(fileHandler, "settings.json")

    // Initialize logger with default settings
    initializeLogger(fileHandler, Settings())

    // Asynchronously load settings and re-initialize the logger
    CoroutineScope(Dispatchers.Default).launch {
        val loadedSettings = settingsManager.loadSettings()
        initializeLogger(fileHandler, loadedSettings)
    }

    val jsonDataManager = JsonDataManager(fileHandler, "stash.json")
    val imageManager = ImageManager(fileHandler)
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
