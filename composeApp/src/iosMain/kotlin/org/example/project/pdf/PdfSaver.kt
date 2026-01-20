package org.example.project.pdf

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberPdfSaver(): (fileName: String, data: ByteArray) -> Unit {
    val window = UIApplication.sharedApplication.windows.lastOrNull() as? UIWindow

    return remember(window) {
        { fileName, data ->
            window?.rootViewController?.let { rootViewController ->
                val nsData = data.usePinned {
                    NSData.create(bytes = it.addressOf(0), length = data.size.toULong())
                }
                val activityViewController = UIActivityViewController(listOf(nsData), null)

                var topController = rootViewController
                while (topController.presentedViewController != null) {
                    topController = topController.presentedViewController!!
                }
                topController.presentViewController(activityViewController, true, null)
            }
        }
    }
}
