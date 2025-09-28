package org.example.openstash

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSUserDomainMask
import platform.Foundation.stringByAppendingPathComponent
import platform.Foundation.writeToFile
import platform.Foundation.writeToURL
import platform.Foundation.readText
import platform.Foundation.NSURL
import platform.Foundation.contentsAtPath
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.dataUsingEncoding
import platform.Foundation.NSUTF8StringEncoding

@OptIn(ExperimentalForeignApi::class)
class IosFileHandler : FileHandler {
    private val fileName = "stash.json"

    private fun getDocumentDirectory(): String {
        val paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true)
        return paths.first() as String
    }

    private fun getFilePath(): String {
        return getDocumentDirectory().stringByAppendingPathComponent(fileName)
    }

    override suspend fun readFile(): String {
        val filePath = getFilePath()
        val fileManager = NSFileManager.defaultManager
        return if (fileManager.fileExistsAtPath(filePath)) {
            // The following is a simplified way to read, might need error handling or encoding specification
            try {
                val nsString = NSString.create(contentsOfFile = filePath, encoding = NSUTF8StringEncoding, error = null)
                nsString?.toString() ?: ""
            } catch (e: Exception) {
                println("Error reading file: ${e.message}")
                ""
            }
        } else {
            ""
        }
    }

    override suspend fun writeFile(content: String) {
        val filePath = getFilePath()
        try {
            (content as NSString).writeToFile(filePath, atomically = true, encoding = NSUTF8StringEncoding, error = null)
        } catch (e: Exception) {
            println("Error writing file: ${e.message}")
        }
    }
}

// Helper to get NSSearchPathForDirectoriesInDomains (not directly exposed in all Kotlin/Native versions the same way)
@OptIn(ExperimentalForeignApi::class)
fun NSSearchPathForDirectoriesInDomains(directory: platform.Foundation.NSSearchPathDirectory, domainMask: platform.Foundation.NSSearchPathDomainMask, expandTilde: kotlin.Boolean): List<*> {
    return platform.Foundation.NSSearchPathForDirectoriesInDomains(directory, domainMask, expandTilde)
}
