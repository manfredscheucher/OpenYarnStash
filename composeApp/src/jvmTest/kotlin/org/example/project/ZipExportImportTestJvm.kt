package org.example.project

import java.io.ByteArrayInputStream
import java.io.File

actual fun createPlatformFileHandler(): FileHandler {
    // Create a temporary directory for testing
    val tempDir = File.createTempFile("openyarnstash_test", "").apply {
        delete()
        mkdirs()
    }
    return JvmFileHandler(tempDir)
}

actual fun ByteArray.toInputStream(): Any {
    return ByteArrayInputStream(this)
}
