package org.example.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val fileHandler = AndroidFileHandler(this)
        val jsonDataManager = JsonDataManager(fileHandler, "stash.json")
        val imageManager = ImageManager(fileHandler)
        val pdfManager = PdfManager(fileHandler)
        val settingsManager = JsonSettingsManager(fileHandler, "settings.json")
        val fileDownloader = FileDownloader(this)

        setContent {
            App(jsonDataManager, imageManager, pdfManager, settingsManager, fileDownloader, fileHandler)
        }
    }
}
