package org.example.project

import androidx.compose.ui.window.CanvasBasedWindow
import org.jetbrains.skiko.wasm.onWasmReady

fun main() {
    val platform = getPlatform()
    val fileHandler = platform.fileHandler
    val jsonDataManager = JsonDataManager(fileHandler, "stash.json")
    val imageManager = ImageManager(fileHandler)
    val settingsManager = JsonSettingsManager(fileHandler, "settings.json")
    val fileDownloader = FileDownloader()

    onWasmReady {
        CanvasBasedWindow("OpenYarnStash") {
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
