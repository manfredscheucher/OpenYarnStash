package org.example.project

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    println("again the question, where do I come from and what do I want?!")
    ComposeViewport {
        App()
    }
}