package org.example.project

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

@Composable
actual fun BackButtonHandler(enabled: Boolean, onBack: () -> Unit) {
    if (!enabled) {
        return
    }
    val backDispatcher = LocalDesktopBackDispatcher.current
    DisposableEffect(onBack) {
        backDispatcher.register(onBack)
        onDispose {
            backDispatcher.unregister(onBack)
        }
    }
}
