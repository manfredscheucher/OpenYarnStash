package org.example.project

actual fun createEmptyImageByteArray(): ByteArray {
    // Not supported on wasm-js
    return ByteArray(0)
}
