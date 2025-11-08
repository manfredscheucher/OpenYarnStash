package org.example.project

import androidx.compose.runtime.Composable

@Composable
actual fun rememberCameraLauncher(onResult: (ByteArray?) -> Unit): CameraLauncher? {
    return null
}
