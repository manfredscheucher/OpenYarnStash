package org.example.project

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
actual fun AppBackHandler(enabled: Boolean, onBack: () -> Unit) {
    val setDesktopBackHandler = LocalDesktopBackHandler.current
    LaunchedEffect(enabled, onBack) {
        setDesktopBackHandler?.invoke(enabled, onBack)
    }
}
