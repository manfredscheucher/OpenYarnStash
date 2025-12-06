package org.example.project

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberCameraLauncher(onResult: (ByteArray?) -> Unit): CameraLauncher? {
    return remember { CameraLauncher(onResult) }
}

actual class CameraLauncher(private val onResult: (ByteArray?) -> Unit) {
    actual fun launch() {
        // Not supported on wasm-js
        onResult(null)
    }
}
