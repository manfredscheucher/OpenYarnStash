package org.example.project

import androidx.compose.runtime.Composable

@Composable
actual fun FilePicker(show: Boolean, onFileSelected: (String?) -> Unit) {
    // Not supported on wasm-js
}

@Composable
actual fun FilePickerForZip(show: Boolean, onFileSelected: (Any?) -> Unit) {
    // Not supported on wasm-js
}
