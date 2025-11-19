package org.example.project.pdf

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.example.project.LogLevel
import org.example.project.Logger
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class PdfThumbnailGeneratorAndroid : PdfThumbnailGenerator {
    override suspend fun generateThumbnail(pdf: ByteArray, width: Int, height: Int): ByteArray? = withContext(Dispatchers.IO) {
        try {
            val tempFile = File.createTempFile("temp_pdf", ".pdf").apply {
                deleteOnExit()
            }
            FileOutputStream(tempFile).use { fos ->
                fos.write(pdf)
            }

            ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                PdfRenderer(pfd).use { renderer ->
                    if (renderer.pageCount > 0) {
                        renderer.openPage(0).use { page ->
                            val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)
                            
                            val outputStream = ByteArrayOutputStream()
                            scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                            outputStream.toByteArray()
                        }
                    } else {
                        null
                    }
                }
            }
        } catch (e: Exception) {
            Logger.log(LogLevel.ERROR, "Failed to generate thumbnail: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}
