package org.example.project

import androidx.compose.runtime.Composable

@Composable
actual fun BackButtonHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS typically uses navigation gestures instead of a back button
    // This can be implemented with UINavigationController if needed
}
