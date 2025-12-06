package org.example.project

import kotlinx.browser.document
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob


@JsFun("data => new Blob([data])")
private external fun createBlob(data: JsAny): Blob

actual class FileDownloader {
    actual fun download(fileName: String, data: ByteArray, context: Any?) {
        val blob = createBlob(data.toJsReference())
        val url = URL.createObjectURL(blob)
        val anchor = document.createElement("a") as HTMLAnchorElement
        anchor.href = url
        anchor.download = fileName
        anchor.click()
        URL.revokeObjectURL(url)
    }
}
