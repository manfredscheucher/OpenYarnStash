package org.example.project

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.PDFRenderer

@Composable
actual fun PdfViewer(
    pdf: ByteArray,
    modifier: Modifier
) {
    val bitmaps = remember {
        val document = Loader.loadPDF(pdf)
        val renderer = PDFRenderer(document)
        val images = (0 until document.numberOfPages).map {
            renderer.renderImageWithDPI(it, 300f)
        }
        document.close()
        images
    }

    Box(modifier = modifier.background(Color.Gray)) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(bitmaps.size) { index ->
                Image(
                    bitmap = bitmaps[index].toComposeImageBitmap(),
                    contentDescription = "PDF page ${index + 1}",
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}