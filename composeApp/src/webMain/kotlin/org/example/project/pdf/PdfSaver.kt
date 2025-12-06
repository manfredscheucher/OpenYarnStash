package org.example.project.pdf

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberPdfSaver(): (String, ByteArray) -> Unit {
    return { _, _ -> // Not supported on wasm-js
    }
}
