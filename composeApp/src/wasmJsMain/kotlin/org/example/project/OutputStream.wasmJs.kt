package org.example.project

actual abstract class OutputStream actual constructor() {
    actual abstract fun write(b: Int)
    actual open fun write(b: ByteArray, off: Int, len: Int) {
        // no-op
    }
    actual open fun flush() {
        // no-op
    }
    actual open fun close() {
        // no-op
    }
}
