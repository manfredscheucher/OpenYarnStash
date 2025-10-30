package org.example.project.pdf

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.browser.document
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag

@Composable
actual fun rememberPdfSaver(): (fileName: String, data: ByteArray) -> Unit {
    return remember {
        { fileName, data ->
            val blob = Blob(arrayOf(data), BlobPropertyBag(type = "application/pdf"))
            val url = URL.createObjectURL(blob)
            val link = document.createElement("a") as HTMLAnchorElement
            link.href = url
            link.download = fileName
            document.body?.appendChild(link) // Required for Firefox
            link.click()
            document.body?.removeChild(link) // Clean up
            URL.revokeObjectURL(url)
        }
    }
}
