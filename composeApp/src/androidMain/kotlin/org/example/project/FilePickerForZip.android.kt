package org.example.project

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import java.io.ByteArrayOutputStream
import java.io.InputStream

@Composable
actual fun FilePickerForZip(show: Boolean, onFileSelected: (ByteArray?) -> Unit) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    onFileSelected(inputStream.readBytes())
                }
            } ?: onFileSelected(null)
        }
    )

    if (show) {
        LaunchedEffect(Unit) {
            launcher.launch("application/zip")
        }
    }
}

// Extension function to read all bytes from an InputStream
private fun InputStream.readBytes(): ByteArray {
    val byteBuffer = ByteArrayOutputStream()
    val buffer = ByteArray(1024)
    var bytesRead: Int
    while (this.read(buffer).also { bytesRead = it } != -1) {
        byteBuffer.write(buffer, 0, bytesRead)
    }
    return byteBuffer.toByteArray()
}
