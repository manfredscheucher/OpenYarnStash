package org.example.project

actual suspend fun resizeImage(bytes: ByteArray, maxWidth: Int, maxHeight: Int): ByteArray {
    // Image resizing for iOS can be implemented using UIImage and CoreGraphics
    // For now, return the original image data
    return bytes
}
