package org.example.project

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZipExportImportTest {

    @Test
    fun testZipExportAndImport() = runBlocking {
        val fileHandler = createPlatformFileHandler()

        // 1. Setup: Create test files
        println("Setting up test files...")
        fileHandler.writeText("hello.txt", "hello world")
        fileHandler.writeText("sub/test.txt", "test content")
        fileHandler.writeText("sub/data.txt", "some data\nline 2\nline 3")

        // Verify files were created
        val content1 = fileHandler.readText("hello.txt")
        assertEquals("hello world", content1, "Initial hello.txt should contain 'hello world'")

        val content2 = fileHandler.readText("sub/test.txt")
        assertEquals("test content", content2, "Initial sub/test.txt should contain 'test content'")

        // 2. Export to ZIP
        println("Exporting to ZIP...")
        val zipBytes = fileHandler.zipFiles()
        assertTrue(zipBytes.isNotEmpty(), "ZIP export should produce non-empty byte array")
        println("ZIP created: ${zipBytes.size} bytes")

        // 3. Clear all files
        println("Clearing files directory...")
        fileHandler.deleteFilesDirectory()

        // Verify files are gone
        val afterDelete = fileHandler.readText("hello.txt")
        assertEquals("", afterDelete, "Files should be deleted after deleteFilesDirectory()")

        // 4. Import from ZIP
        println("Importing from ZIP...")
        val inputStream = zipBytes.toInputStream()
        fileHandler.unzipAndReplaceFiles(inputStream)

        // 5. Verify imported files
        println("Verifying imported files...")
        val imported1 = fileHandler.readText("hello.txt")
        assertEquals("hello world", imported1, "Imported hello.txt should contain 'hello world'")

        val imported2 = fileHandler.readText("sub/test.txt")
        assertEquals("test content", imported2, "Imported sub/test.txt should contain 'test content'")

        val imported3 = fileHandler.readText("sub/data.txt")
        assertEquals("some data\nline 2\nline 3", imported3, "Imported sub/data.txt should match original")

        // Cleanup
        println("Test passed! Cleaning up...")
        fileHandler.deleteFilesDirectory()

        println("✓ ZIP Export/Import test completed successfully!")
    }

    @Test
    fun testEmptyZipExport() = runBlocking {
        val fileHandler = createPlatformFileHandler()

        // Clear everything first
        fileHandler.deleteFilesDirectory()

        // Export empty directory
        val zipBytes = fileHandler.zipFiles()

        // Should create valid (possibly empty) ZIP
        println("Empty ZIP size: ${zipBytes.size} bytes")

        println("✓ Empty ZIP export test completed!")
    }

    @Test
    fun testBinaryDataInZip() = runBlocking {
        val fileHandler = createPlatformFileHandler()

        // Create binary test data
        val binaryData = ByteArray(256) { it.toByte() }
        fileHandler.writeBytes("binary.dat", binaryData)

        // Export
        val zipBytes = fileHandler.zipFiles()
        assertTrue(zipBytes.isNotEmpty(), "ZIP should contain binary data")

        // Clear and import
        fileHandler.deleteFilesDirectory()
        val inputStream = zipBytes.toInputStream()
        fileHandler.unzipAndReplaceFiles(inputStream)

        // Verify binary data
        val imported = fileHandler.readBytes("binary.dat")
        assertTrue(imported != null, "Binary file should be imported")
        assertEquals(256, imported!!.size, "Binary data size should match")

        for (i in binaryData.indices) {
            assertEquals(binaryData[i], imported[i], "Binary data at index $i should match")
        }

        // Cleanup
        fileHandler.deleteFilesDirectory()

        println("✓ Binary data ZIP test completed!")
    }
}

// Platform-specific FileHandler creation
expect fun createPlatformFileHandler(): FileHandler

// Helper to convert ByteArray to platform-specific InputStream
expect fun ByteArray.toInputStream(): Any
