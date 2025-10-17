package org.example.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        CONTEXT = this
        val repo = JsonRepository(AndroidFileHandler(this))

        setContent {
            CompositionLocalProvider(
                LocalFileDownloader provides AndroidFileDownloader(this)
            ) {
                App(repo)
            }
        }
    }
}
