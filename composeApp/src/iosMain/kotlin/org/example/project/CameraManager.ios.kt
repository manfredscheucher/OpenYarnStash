package org.example.project

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.refTo
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSData
import platform.Foundation.NSProcessInfo
import platform.UIKit.UIApplication
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetCurrentContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.darwin.NSObject
import platform.posix.memcpy

@Composable
actual fun rememberCameraLauncher(onResult: (ByteArray?) -> Unit): CameraLauncher? {
    return remember { IosCameraLauncher(onResult) }
}

class IosCameraLauncher(private val onResult: (ByteArray?) -> Unit) : CameraLauncher {
    override fun launch() {
        // Check if running in simulator
        val isSimulator = isRunningInSimulator()

        if (isSimulator) {
            // Generate a test image for simulator
            val testImage = createTestImage()
            val imageData = UIImageJPEGRepresentation(testImage, 0.9)
            if (imageData != null) {
                val bytes = imageData.toByteArray()
                onResult(bytes)
            } else {
                onResult(null)
            }
            return
        }

        val picker = UIImagePickerController()
        picker.sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera

        val delegate = object : NSObject(),
            UIImagePickerControllerDelegateProtocol,
            UINavigationControllerDelegateProtocol {

            @OptIn(ExperimentalForeignApi::class)
            override fun imagePickerController(
                picker: UIImagePickerController,
                didFinishPickingMediaWithInfo: Map<Any?, *>
            ) {
                val image = didFinishPickingMediaWithInfo["UIImagePickerControllerOriginalImage"]
                    as? platform.UIKit.UIImage

                if (image != null) {
                    val imageData = UIImageJPEGRepresentation(image, 0.9)
                    if (imageData != null) {
                        val bytes = imageData.toByteArray()
                        onResult(bytes)
                    } else {
                        onResult(null)
                    }
                } else {
                    onResult(null)
                }

                picker.dismissViewControllerAnimated(true, null)
            }

            override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
                onResult(null)
                picker.dismissViewControllerAnimated(true, null)
            }
        }

        picker.delegate = delegate

        val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
        rootViewController?.presentViewController(picker, animated = true, completion = null)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val size = this.length.toInt()
    val bytes = ByteArray(size)
    if (size > 0) {
        memcpy(bytes.refTo(0), this.bytes, this.length)
    }
    return bytes
}

private fun isRunningInSimulator(): Boolean {
    val environment = NSProcessInfo.processInfo.environment
    return environment["SIMULATOR_DEVICE_NAME"] != null
}

@OptIn(ExperimentalForeignApi::class)
private fun createTestImage(): UIImage {
    val width = 400.0
    val height = 400.0

    UIGraphicsBeginImageContextWithOptions(
        size = platform.CoreGraphics.CGSizeMake(width, height),
        opaque = false,
        scale = 1.0
    )

    val context = UIGraphicsGetCurrentContext()

    // Draw a simple gradient/pattern as test image
    if (context != null) {
        // Fill background with a color
        platform.CoreGraphics.CGContextSetRGBFillColor(context, 0.2, 0.4, 0.8, 1.0)
        platform.CoreGraphics.CGContextFillRect(context, CGRectMake(0.0, 0.0, width, height))

        // Draw some shapes to make it recognizable
        platform.CoreGraphics.CGContextSetRGBFillColor(context, 1.0, 0.5, 0.0, 1.0)
        platform.CoreGraphics.CGContextFillEllipseInRect(context, CGRectMake(100.0, 100.0, 200.0, 200.0))

        platform.CoreGraphics.CGContextSetRGBFillColor(context, 1.0, 1.0, 1.0, 1.0)
        platform.CoreGraphics.CGContextFillRect(context, CGRectMake(150.0, 150.0, 100.0, 100.0))
    }

    val image = UIGraphicsGetImageFromCurrentImageContext()
    UIGraphicsEndImageContext()

    return image ?: UIImage()
}
