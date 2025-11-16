package org.example.project.pdf

actual object PdfThumbnailGeneratorFactory {
    actual fun create(): PdfThumbnailGenerator {
        return PdfThumbnailGeneratorIos()
    }
}
