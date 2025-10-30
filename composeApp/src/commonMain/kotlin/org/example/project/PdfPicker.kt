package org.example.project

import androidx.compose.runtime.Composable

@Composable
expect fun rememberPdfPickerLauncher(onPdfSelected: (ByteArray?) -> Unit): (String) -> Unit
