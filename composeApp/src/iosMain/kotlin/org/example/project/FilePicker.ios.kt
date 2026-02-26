package org.example.project

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.interop.LocalUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.*
import platform.UniformTypeIdentifiers.UTTypeJSON
import platform.UniformTypeIdentifiers.UTTypeZIP
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.darwin.NSObject

@Composable
actual fun FilePicker(show: Boolean, onFileSelected: (String?) -> Unit) {
    // iOS file picker can be implemented with UIDocumentPickerViewController
    // For now, return null
    LaunchedEffect(show) {
        if (show) {
            onFileSelected(null)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun FilePickerForZip(show: Boolean, onFileSelected: (Any?) -> Unit) {
    val viewController = LocalUIViewController.current

    val delegate = remember {
        object : NSObject(), UIDocumentPickerDelegateProtocol {
            override fun documentPicker(
                controller: UIDocumentPickerViewController,
                didPickDocumentsAtURLs: List<*>
            ) {
                val urls = didPickDocumentsAtURLs as? List<NSURL>
                val url = urls?.firstOrNull()

                if (url != null) {
                    // Start accessing the security-scoped resource
                    url.startAccessingSecurityScopedResource()

                    try {
                        // Create an input stream from the URL
                        val inputStream = NSInputStream.inputStreamWithURL(url)
                        onFileSelected(inputStream)
                    } finally {
                        // Stop accessing the security-scoped resource
                        url.stopAccessingSecurityScopedResource()
                    }
                } else {
                    onFileSelected(null)
                }
            }

            override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
                onFileSelected(null)
            }
        }
    }

    LaunchedEffect(show) {
        if (show) {
            val picker = UIDocumentPickerViewController(
                forOpeningContentTypes = listOf(UTTypeZIP),
                asCopy = true
            )
            picker.delegate = delegate
            picker.allowsMultipleSelection = false

            viewController.presentViewController(picker, animated = true, completion = null)
        }
    }
}
