package org.example.project

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport


@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val repo = JsonRepository(JsFileHandler())
    ComposeViewport {
        CompositionLocalProvider(
            LocalFileDownloader provides JsFileDownloader()
        ) {
            App(repo)
        }
    }
}
