package org.example.project

import androidx.compose.runtime.Composable

@Composable
actual fun rememberPdfPickerLauncher(onPdfSelected: (ByteArray?) -> Unit): (String) -> Unit {
    return { _: String -> // Not supported on wasm-js 
        onPdfSelected(null)
    }
}
