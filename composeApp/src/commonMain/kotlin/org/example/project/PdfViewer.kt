package org.example.project

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun PdfViewer(
    pdf: ByteArray,
    modifier: Modifier = Modifier
)
