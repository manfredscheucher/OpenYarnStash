package org.example.project.pdf

import androidx.compose.runtime.Composable

@Composable
expect fun rememberPdfSaver(): (fileName: String, data: ByteArray) -> Unit
