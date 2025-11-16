package org.example.project.pdf

import android.content.Context

actual object PdfThumbnailGeneratorFactory {
    actual fun create(): PdfThumbnailGenerator {
        return PdfThumbnailGeneratorAndroid()
    }
}
