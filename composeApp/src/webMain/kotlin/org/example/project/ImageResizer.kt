package org.example.project

actual suspend fun resizeImage(bytes: ByteArray, maxWidth: Int, maxHeight: Int): ByteArray {
    // Not supported on wasm-js
    return bytes
}
