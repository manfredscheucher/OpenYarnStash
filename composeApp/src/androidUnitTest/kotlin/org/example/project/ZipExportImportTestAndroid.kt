package org.example.project

import java.io.ByteArrayInputStream

actual fun createPlatformFileHandler(): FileHandler {
    // For unit tests, use JVM handler (Android instrumented tests would use real AndroidFileHandler)
    return JvmFileHandler()
}

actual fun ByteArray.toInputStream(): Any {
    return ByteArrayInputStream(this)
}
