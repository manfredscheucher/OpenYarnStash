package org.example.project

actual open class OutputStream actual constructor() {
    open fun write(b: Int) {
        // no-op
    }

    open fun write(b: ByteArray, off: Int, len: Int) {
        // no-op
    }

    open fun flush() {
        // no-op
    }

    open fun close() {
        // no-op
    }
}
