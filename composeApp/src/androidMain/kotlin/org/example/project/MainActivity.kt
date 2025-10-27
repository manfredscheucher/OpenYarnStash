package org.example.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fileHandler = AndroidFileHandler(this)
        val jsonDataManager = JsonDataManager(fileHandler, "stash.json")
        val imageManager = ImageManager(fileHandler)
        val settingsManager = JsonSettingsManager(fileHandler, "settings.json")

        setContent {
            CompositionLocalProvider(
                LocalFileDownloader provides AndroidFileDownloader(this)
            ) {
                App(jsonDataManager, imageManager, settingsManager)
            }
        }
    }
}
