package org.example.project

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

@Composable
actual fun rememberCameraLauncher(onResult: (ByteArray?) -> Unit): CameraLauncher? {
    val context = LocalContext.current
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            photoUri?.let { uri ->
                context.contentResolver.openInputStream(uri)?.use {
                    onResult(it.readBytes())
                }
            }
        } else {
            onResult(null)
        }
    }

    return remember(launcher) {
        object : CameraLauncher {
            override fun launch() {
                val file = createImageFile(context)
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                photoUri = uri
                launcher.launch(uri)
            }
        }
    }
}

private fun createImageFile(context: Context): File {
    val timeStamp = System.currentTimeMillis()
    val storageDir: File? = context.getExternalFilesDir(null)
    return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
}
