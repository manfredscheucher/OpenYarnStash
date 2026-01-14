package org.example.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

actual fun initializeLogger(fileHandler: FileHandler, settings: Settings) {
    Logger.init(fileHandler, settings)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize context for file downloads
        setContext(this)

        val platform = getPlatform(this)
        val fileHandler = platform.fileHandler
        val jsonDataManager = JsonDataManager(fileHandler, "stash.json")
        val imageManager = ImageManager(fileHandler)
        val settingsManager = JsonSettingsManager(fileHandler, "settings.json")
        val fileDownloader = FileDownloader()

        initializeLogger(fileHandler, settingsManager.settings)

        setContent {
            App(jsonDataManager, imageManager, fileDownloader, fileHandler, settingsManager)
        }
    }
}
