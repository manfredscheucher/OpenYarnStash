package org.example.project

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

actual class ImagePickerLauncher(
    private val launcher: androidx.activity.result.ActivityResultLauncher<String>
) {
    actual fun launch() {
        launcher.launch("image/*")
    }
}

@Composable
actual fun rememberImagePickerLauncher(onImageSelected: (ByteArray) -> Unit): ImagePickerLauncher {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.use { inputStream ->
                val resizedImage = resizeImage(inputStream.readBytes(), 400, 400)
                onImageSelected(resizedImage)
            }
        }
    }
    return remember { ImagePickerLauncher(launcher) }
}
