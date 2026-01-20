package org.example.project

actual fun createPdfManager(fileHandler: FileHandler): PdfManager {
    return PdfManager(fileHandler)
}
