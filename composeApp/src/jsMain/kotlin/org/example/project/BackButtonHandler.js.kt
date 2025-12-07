package org.example.project

import androidx.compose.runtime.Composable

@Composable
actual fun BackButtonHandler(enabled: Boolean, onBack: () -> Unit) {
    // In browsers, back button handling is typically not needed
    // Browser's native back button is used instead
}
