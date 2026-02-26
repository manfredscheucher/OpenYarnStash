package org.example.project

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.refTo
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
actual fun rememberImagePickerLauncher(onImagesSelected: (List<ByteArray>) -> Unit): ImagePickerLauncher {
    return remember { IosImagePickerLauncher(onImagesSelected) }
}

class IosImagePickerLauncher(private val onImagesSelected: (List<ByteArray>) -> Unit) : ImagePickerLauncher {
    override fun launch() {
        val picker = UIImagePickerController()
        picker.sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
        picker.allowsEditing = false

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
                        onImagesSelected(listOf(bytes))
                    } else {
                        onImagesSelected(emptyList())
                    }
                } else {
                    onImagesSelected(emptyList())
                }

                picker.dismissViewControllerAnimated(true, null)
            }

            override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
                onImagesSelected(emptyList())
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
