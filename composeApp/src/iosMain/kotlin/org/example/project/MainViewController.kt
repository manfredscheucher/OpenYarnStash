package org.example.project

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController {
    val fileHandler = IosFileHandler()
    val jsonDataManager = JsonDataManager(fileHandler, "stash.json")
    val imageManager = ImageManager(fileHandler)
    val pdfManager = PdfManager(fileHandler)
    val settingsManager = JsonSettingsManager(fileHandler, "settings.json")
    val fileDownloader = FileDownloader()

    App(jsonDataManager, imageManager, pdfManager, settingsManager, fileDownloader, fileHandler)
}