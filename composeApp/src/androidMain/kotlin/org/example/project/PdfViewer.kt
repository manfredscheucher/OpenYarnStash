package org.example.project

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import java.io.File

@Composable
actual fun PdfViewer(
    pdf: ByteArray,
    modifier: Modifier
) {
    val file = File.createTempFile("pattern", ".pdf")
    file.writeBytes(pdf)

    val fileDescriptor by remember {
        derivedStateOf {
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        }
    }
    val pdfRenderer by remember {
        derivedStateOf {
            PdfRenderer(fileDescriptor)
        }
    }

    var bitmaps by remember { mutableStateOf<List<Bitmap>>(emptyList()) }

    DisposableEffect(Unit) {
        onDispose {
            pdfRenderer.close()
            fileDescriptor.close()
            file.delete()
        }
    }

    if (bitmaps.isEmpty()) {
        val newBitmaps = mutableListOf<Bitmap>()
        for (i in 0 until pdfRenderer.pageCount) {
            val page = pdfRenderer.openPage(i)
            val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            newBitmaps.add(bitmap)
            page.close()
        }
        bitmaps = newBitmaps
    }

    Box(modifier = modifier.background(Color.Gray)) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(bitmaps.size) { index ->
                Image(
                    bitmap = bitmaps[index].asImageBitmap(),
                    contentDescription = "PDF page ${index + 1}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillWidth
                )
            }
        }
    }
}