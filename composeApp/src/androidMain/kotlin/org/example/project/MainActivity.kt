package org.example.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import kotlinx.coroutines.runBlocking

actual fun initializeLogger(fileHandler: FileHandler, settings: Settings) {
    Logger.init(fileHandler, settings)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val platform = getPlatform(this)
        val fileHandler = platform.fileHandler
        val settingsManager = JsonSettingsManager(fileHandler, "settings.json")

        // Initialize logger with default settings to allow logging during settings load
        initializeLogger(fileHandler, Settings())

        // Load settings synchronously before the UI starts
        val loadedSettings = runBlocking {
            settingsManager.loadSettings()
        }
        // Re-initialize the logger with the loaded settings
        initializeLogger(fileHandler, loadedSettings)

        val jsonDataManager = JsonDataManager(fileHandler, "stash.json")
        val imageManager = ImageManager(fileHandler)
        val fileDownloader = FileDownloader()

        setContent {
            App(jsonDataManager, imageManager, fileDownloader, fileHandler, settingsManager)
        }
    }
}
