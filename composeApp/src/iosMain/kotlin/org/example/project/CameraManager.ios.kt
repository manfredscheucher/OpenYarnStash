package org.example.project

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.refTo
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSData
import platform.UIKit.UIApplication
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
