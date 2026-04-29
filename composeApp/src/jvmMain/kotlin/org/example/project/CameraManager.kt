package org.example.project

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberCameraLauncher(onResult: (ByteArray?) -> Unit): CameraLauncher? {
    return remember { JvmCameraLauncher(onResult) }
}

class JvmCameraLauncher(private val onResult: (ByteArray?) -> Unit) : CameraLauncher {
    override fun launch() {
        // Not supported on JVM
        onResult(null)
    }
}
