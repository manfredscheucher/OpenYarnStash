package org.example.project

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSInputStream
import platform.Foundation.create

actual fun createPlatformFileHandler(): FileHandler {
    return IosFileHandler()
}

@OptIn(ExperimentalForeignApi::class)
actual fun ByteArray.toInputStream(): Any {
    val nsData = this.usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = this.size.toULong())
    }
    return NSInputStream(data = nsData)
}
