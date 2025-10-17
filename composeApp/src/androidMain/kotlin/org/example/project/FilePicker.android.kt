package org.example.project

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun FilePicker(show: Boolean, onFileSelected: (String?) -> Unit) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    onFileSelected(inputStream.reader().readText())
                }
            } ?: onFileSelected(null)
        }
    )

    if (show) {
        LaunchedEffect(Unit) {
            launcher.launch("application/json")
        }
    }
}
