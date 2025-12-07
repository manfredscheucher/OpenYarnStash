package org.example.project.pdf

actual object PdfThumbnailGeneratorFactory {
    actual fun create(): PdfThumbnailGenerator {
        return object : PdfThumbnailGenerator {
            override suspend fun generateThumbnail(pdf: ByteArray, width: Int, height: Int): ByteArray? {
                throw NotImplementedError("PDF thumbnail generation is not yet implemented for JS")
            }
        }
    }
}
