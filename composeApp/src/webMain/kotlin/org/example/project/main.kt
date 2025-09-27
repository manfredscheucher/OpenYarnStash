package org.example.project

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    println("auch hier wieder die frage, wo ich denn herkomme und was ich denn möchte?!")
    ComposeViewport {
        App()
    }
}