package org.example.project

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

@Composable
actual fun BackButtonHandler(enabled: Boolean, onBack: () -> Unit) {
    BackHandler(enabled, onBack)
}
