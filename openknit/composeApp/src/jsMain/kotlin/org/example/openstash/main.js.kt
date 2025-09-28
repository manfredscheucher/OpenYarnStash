package org.example.openstash

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CanvasBasedWindow(canvasElementId = "ComposeTarget") { // "ComposeTarget" is the default ID in index.html
        App(JsonRepository(JsFileHandler()))
    }
}
