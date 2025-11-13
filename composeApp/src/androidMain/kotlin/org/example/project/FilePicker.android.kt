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
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                context.contentResolver.openInputStream(it)?.use { stream ->
                    onFileSelected(stream.bufferedReader().readText())
                }
            } ?: onFileSelected(null)
        }
    )

    LaunchedEffect(show) {
        if (show) {
            launcher.launch(arrayOf("application/json"))
        }
    }
}

@Composable
actual fun FilePickerForZip(show: Boolean, onFileSelected: (Any?) -> Unit) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                context.contentResolver.openInputStream(it)?.let {
                    onFileSelected(it)
                }
            } ?: onFileSelected(null)
        }
    )

    LaunchedEffect(show) {
        if (show) {
            launcher.launch(arrayOf("application/zip"))
        }
    }
}
