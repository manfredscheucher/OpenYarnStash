package org.example.project

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberCameraLauncher(onResult: (ByteArray?) -> Unit): CameraLauncher? {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            // TODO: get the image data and call onResult
        } else {
            onResult(null)
        }
    }
    return remember { CameraLauncher(launcher) }
}

actual class CameraLauncher(private val launcher: androidx.activity.result.ActivityResultLauncher<android.net.Uri>) {
    actual fun launch() {
        // TODO: create a temporary file and pass the URI to the launcher
    }
}
