package org.example.project

import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.url.URL
import kotlinx.browser.document

class JsFileDownloader : FileDownloader {
    override fun download(fileName: String, data: String) {
        val blob = org.w3c.files.Blob(arrayOf(data), org.w3c.files.BlobPropertyBag(type = "application/json"))
        val url = URL.createObjectURL(blob)
        val a = document.createElement("a") as HTMLAnchorElement
        a.href = url
        a.download = fileName
        a.click()
        URL.revokeObjectURL(url)
    }
}
