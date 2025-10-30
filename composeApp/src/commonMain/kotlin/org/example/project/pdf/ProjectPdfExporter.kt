package org.example.project.pdf

interface ProjectPdfExporter {
    /**
     * Erzeugt das PDF als ByteArray (A4). Speichern/Teilen übernimmt der Aufrufer.
     */
    suspend fun exportToPdf(
        project: Project,
        params: Params,
        yarns: List<YarnUsage>
    ): ByteArray
}
