package org.example.project

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberPdfPickerLauncher(onPdfSelected: (ByteArray?) -> Unit): (String) -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                context.contentResolver.openInputStream(it)?.use {
                    onPdfSelected(it.readBytes())
                }
            } ?: onPdfSelected(null)
        }
    )
    return { mimeType -> launcher.launch(mimeType) }
}
