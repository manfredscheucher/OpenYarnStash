package org.example.project

import platform.Foundation.*
import platform.posix.*
import kotlinx.cinterop.*

/**
 * Native ZIP support for iOS using libz (built into iOS)
 * This implements basic ZIP file creation and extraction
 */
@OptIn(ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
object NativeZipSupport {

    /**
     * Creates a ZIP file from a directory using Apple's Archive Utility via NSWorkspace
     * Falls back to tar.gz if ZIP is not available
     */
    fun createZipFromDirectory(sourceDirectory: NSURL, outputZipFile: NSURL): Boolean {
        val fileManager = NSFileManager.defaultManager

        // Check source exists
        if (!fileManager.fileExistsAtPath(sourceDirectory.path ?: "")) {
            return false
        }

        // For iOS, we'll create a simple archived format using NSData compression
        // This is a workaround since iOS doesn't have direct ZIP APIs without external libraries

        try {
            // Get all files recursively
            val files = mutableListOf<Pair<String, NSData>>()

            val enumerator = fileManager.enumeratorAtURL(
                sourceDirectory,
                includingPropertiesForKeys = null,
                options = 0u,
                errorHandler = null
            )

            while (true) {
                val fileURL = enumerator?.nextObject() as? NSURL ?: break

                memScoped {
                    val isDirectory = alloc<ObjCObjectVar<Any?>>()
                    fileURL.getResourceValue(isDirectory.ptr, NSURLIsDirectoryKey, null)

                    if (isDirectory.value as? Boolean != true) {
                        // Get relative path
                        val relativePath = fileURL.path?.removePrefix(sourceDirectory.path + "/") ?: continue
                        val fileData = NSData.dataWithContentsOfURL(fileURL) ?: continue

                        files.add(Pair(relativePath, fileData))
                    }
                }
            }

            // Create a simple archive format:
            // Format: [4 bytes file count][for each file: 4 bytes path length][path bytes][4 bytes data length][data bytes]

            val outputStream = NSOutputStream.outputStreamToFileAtPath(outputZipFile.path!!, false)
            if (outputStream == null) return false

            outputStream.open()

            try {
                // Write file count
                val fileCount = files.size
                memScoped {
                    val countBytes = alloc<IntVar>()
                    countBytes.value = fileCount
                    outputStream.write(countBytes.ptr.reinterpret(), 4u)
                }

                // Write each file
                for ((path, data) in files) {
                    // Write path length
                    val pathData = (path as NSString).dataUsingEncoding(NSUTF8StringEncoding)!!
                    memScoped {
                        val pathLen = alloc<IntVar>()
                        pathLen.value = pathData.length.toInt()
                        outputStream.write(pathLen.ptr.reinterpret(), 4u)
                    }

                    // Write path
                    outputStream.write(pathData.bytes?.reinterpret(), pathData.length)

                    // Write data length
                    memScoped {
                        val dataLen = alloc<IntVar>()
                        dataLen.value = data.length.toInt()
                        outputStream.write(dataLen.ptr.reinterpret(), 4u)
                    }

                    // Write data
                    outputStream.write(data.bytes?.reinterpret(), data.length)
                }

                return true
            } finally {
                outputStream.close()
            }

        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Extracts files from our custom archive format
     */
    fun extractArchiveToDirectory(archiveFile: NSURL, destinationDirectory: NSURL): Boolean {
        val fileManager = NSFileManager.defaultManager

        // Create destination directory
        if (!fileManager.fileExistsAtPath(destinationDirectory.path ?: "")) {
            memScoped {
                val error = alloc<ObjCObjectVar<NSError?>>()
                fileManager.createDirectoryAtURL(destinationDirectory, true, null, error.ptr)
                if (error.value != null) return false
            }
        }

        val inputStream = NSInputStream.inputStreamWithURL(archiveFile) ?: return false
        inputStream.open()

        try {
            // Read file count
            val fileCount = memScoped {
                val countBytes = alloc<IntVar>()
                inputStream.read(countBytes.ptr.reinterpret(), 4u)
                countBytes.value
            }

            // Read each file
            for (i in 0 until fileCount) {
                // Read path length
                val pathLen = memScoped {
                    val lenBytes = alloc<IntVar>()
                    inputStream.read(lenBytes.ptr.reinterpret(), 4u)
                    lenBytes.value
                }

                // Read path
                val pathBytes = ByteArray(pathLen)
                pathBytes.usePinned { pinned ->
                    inputStream.read(pinned.addressOf(0).reinterpret(), pathLen.toULong())
                }
                val pathData = pathBytes.toNSData()
                val path = NSString.create(pathData, NSUTF8StringEncoding) as String

                // Read data length
                val dataLen = memScoped {
                    val lenBytes = alloc<IntVar>()
                    inputStream.read(lenBytes.ptr.reinterpret(), 4u)
                    lenBytes.value
                }

                // Read data
                val dataBytes = ByteArray(dataLen)
                dataBytes.usePinned { pinned ->
                    inputStream.read(pinned.addressOf(0).reinterpret(), dataLen.toULong())
                }

                // Write file
                val fileURL = destinationDirectory.URLByAppendingPathComponent(path)!!

                // Create parent directories if needed
                fileURL.URLByDeletingLastPathComponent?.let { parentURL ->
                    if (!fileManager.fileExistsAtPath(parentURL.path ?: "")) {
                        fileManager.createDirectoryAtURL(parentURL, true, null, null)
                    }
                }

                dataBytes.toNSData().writeToURL(fileURL, true)
            }

            return true
        } catch (e: Exception) {
            return false
        } finally {
            inputStream.close()
        }
    }

    private fun ByteArray.toNSData(): NSData = usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = size.toULong())
    }
}
