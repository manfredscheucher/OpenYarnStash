package org.example.project

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController

@OptIn(ExperimentalForeignApi::class)
actual class FileDownloader {
    actual fun download(fileName: String, data: String, context: Any?) {
        val nsString = data as platform.Foundation.NSString
        val nsData = nsString.dataUsingEncoding(NSUTF8StringEncoding)
        if (nsData != null) {
            shareFile(nsData)
        }
    }

    actual fun download(fileName: String, data: ByteArray, context: Any?) {
        val nsData = data.usePinned {
            NSData.create(bytes = it.addressOf(0), length = data.size.toULong())
        }
        shareFile(nsData)
    }

    private fun shareFile(data: NSData) {
        val scenes = UIApplication.sharedApplication.connectedScenes
        val windowScene = scenes.firstOrNull()
        if (windowScene != null) {
            // Get the key window from the window scene
            val windows = windowScene as? platform.UIKit.UIWindowScene
            val window = windows?.windows?.firstOrNull() as? platform.UIKit.UIWindow
            val rootVC = window?.rootViewController as? UIViewController

            rootVC?.let { vc ->
                val activityVC = UIActivityViewController(listOf(data), null)
                var topVC: UIViewController = vc
                while (topVC.presentedViewController != null) {
                    topVC = topVC.presentedViewController as UIViewController
                }
                topVC.presentViewController(activityVC, animated = true, completion = null)
            }
        }
    }
}
