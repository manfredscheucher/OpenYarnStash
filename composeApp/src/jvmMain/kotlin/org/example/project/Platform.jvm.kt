package org.example.project

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
    override val fileHandler: FileHandler = JvmFileHandler()
}

actual fun getPlatform(context: Any?): Platform = JVMPlatform()
