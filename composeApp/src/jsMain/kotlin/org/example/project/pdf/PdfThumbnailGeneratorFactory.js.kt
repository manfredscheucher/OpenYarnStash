package org.example.project.pdf

actual object PdfThumbnailGeneratorFactory {
    actual fun create(): PdfThumbnailGenerator {
        return object : PdfThumbnailGenerator {
            override suspend fun generateThumbnail(pdf: ByteArray, width: Int, height: Int): ByteArray? {
                // PDF thumbnail generation not supported in browser (would require PDF.js)
                // Return null so the base PdfManager handles it gracefully
                return null
            }
        }
    }
}
