package org.example.project

import kotlinx.browser.document
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob

actual class FileDownloader {
    actual fun download(fileName: String, data: String, context: Any?) {
        val blob = Blob(arrayOf(data))
        val url = URL.createObjectURL(blob)
        val anchor = document.createElement("a") as HTMLAnchorElement
        anchor.href = url
        anchor.download = fileName
        anchor.click()
        URL.revokeObjectURL(url)
    }

    actual fun download(fileName: String, data: ByteArray, context: Any?) {
        val blob = Blob(arrayOf(data))
        val url = URL.createObjectURL(blob)
        val anchor = document.createElement("a") as HTMLAnchorElement
        anchor.href = url
        anchor.download = fileName
        anchor.click()
        URL.revokeObjectURL(url)
    }
}
