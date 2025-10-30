package org.example.project.pdf

actual fun getProjectPdfExporter(): ProjectPdfExporter {
    return ProjectPdfExporterIos()
}
