package org.example.project.pdf

interface PdfThumbnailGenerator {
    suspend fun generateThumbnail(pdf: ByteArray, width: Int, height: Int): ByteArray?
}
