package org.example.project.pdf

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberPdfSaver(): (fileName: String, data: ByteArray) -> Unit {
    val context = LocalContext.current
    var pdfData by remember { mutableStateOf<ByteArray?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf"),
        onResult = { uri ->
            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    pdfData?.let { data -> outputStream.write(data) }
                }
            }
        }
    )

    return remember(launcher) {
        { fileName, data ->
            pdfData = data
            launcher.launch(fileName)
        }
    }
}
