package org.example.project

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@OptIn(ExperimentalForeignApi::class)
actual class FileDownloader {
    actual fun download(fileName: String, data: String, context: Any?) {
        val nsString = data as platform.Foundation.NSString
        val nsData = nsString.dataUsingEncoding(NSUTF8StringEncoding)
        if (nsData != null) {
            dispatch_async(dispatch_get_main_queue()) {
                shareFile(fileName, nsData)
            }
        }
    }

    actual fun download(fileName: String, data: ByteArray, context: Any?) {
        val nsData = data.usePinned {
            NSData.create(bytes = it.addressOf(0), length = data.size.toULong())
        }
        dispatch_async(dispatch_get_main_queue()) {
            shareFile(fileName, nsData)
        }
    }

    @OptIn(kotlinx.cinterop.BetaInteropApi::class)
    private fun shareFile(fileName: String, data: NSData) {
        // Create temporary file with proper filename
        val tempDir = NSTemporaryDirectory()
        val fileURL = NSURL.fileURLWithPath(tempDir + fileName)

        // Write data to temp file using FileManager
        val fileManager = NSFileManager.defaultManager
        fileManager.createFileAtPath(
            path = fileURL.path ?: return,
            contents = data,
            attributes = null
        )

        val scenes = UIApplication.sharedApplication.connectedScenes
        val windowScene = scenes.firstOrNull()
        if (windowScene != null) {
            // Get the key window from the window scene
            val windows = windowScene as? platform.UIKit.UIWindowScene
            val window = windows?.windows?.firstOrNull() as? platform.UIKit.UIWindow
            val rootVC = window?.rootViewController

            if (rootVC != null) {
                val activityVC = UIActivityViewController(listOf(fileURL), null)
                var topVC: UIViewController = rootVC
                while (topVC.presentedViewController != null) {
                    topVC = topVC.presentedViewController!!
                }
                topVC.presentViewController(activityVC, true, null)
            }
        }
    }
}
