package org.example.project.pdf

actual object PdfThumbnailGeneratorFactory {
    actual fun create(): PdfThumbnailGenerator {
        return object : PdfThumbnailGenerator {
            override suspend fun generateThumbnail(pdfBytes: ByteArray, width: Int, height: Int): ByteArray? {
                return null
            }
        }
    }
}
