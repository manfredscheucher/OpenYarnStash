package org.example.project

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.collections.shouldNotBeEmpty

class ZipExportImportTest : FunSpec({

    test("ZIP export and import should preserve all files") {
        val fileHandler = createPlatformFileHandler()

        // 1. Setup: Create test files
        println("Setting up test files...")
        fileHandler.writeText("hello.txt", "hello world")
        fileHandler.writeText("sub/test.txt", "test content")
        fileHandler.writeText("sub/data.txt", "some data\nline 2\nline 3")

        // Verify files were created
        val content1 = fileHandler.readText("hello.txt")
        content1 shouldBe "hello world"

        val content2 = fileHandler.readText("sub/test.txt")
        content2 shouldBe "test content"

        // 2. Export to ZIP
        println("Exporting to ZIP...")
        val zipBytes = fileHandler.zipFiles()
        zipBytes.shouldNotBeEmpty()
        println("ZIP created: ${zipBytes.size} bytes")

        // 3. Clear all files
        println("Clearing files directory...")
        fileHandler.deleteFilesDirectory()

        // Verify files are gone
        val afterDelete = fileHandler.readText("hello.txt")
        afterDelete shouldBe ""

        // 4. Import from ZIP
        println("Importing from ZIP...")
        val inputStream = zipBytes.toInputStream()
        fileHandler.unzipAndReplaceFiles(inputStream)

        // 5. Verify imported files
        println("Verifying imported files...")
        val imported1 = fileHandler.readText("hello.txt")
        imported1 shouldBe "hello world"

        val imported2 = fileHandler.readText("sub/test.txt")
        imported2 shouldBe "test content"

        val imported3 = fileHandler.readText("sub/data.txt")
        imported3 shouldBe "some data\nline 2\nline 3"

        // Cleanup
        println("Test passed! Cleaning up...")
        fileHandler.deleteFilesDirectory()

        println("✓ ZIP Export/Import test completed successfully!")
    }

    test("empty ZIP export should succeed") {
        val fileHandler = createPlatformFileHandler()

        // Clear everything first
        fileHandler.deleteFilesDirectory()

        // Export empty directory
        val zipBytes = fileHandler.zipFiles()

        // Should create valid (possibly empty) ZIP
        println("Empty ZIP size: ${zipBytes.size} bytes")

        println("✓ Empty ZIP export test completed!")
    }

    test("binary data should survive ZIP export/import") {
        val fileHandler = createPlatformFileHandler()

        // Create binary test data
        val binaryData = ByteArray(256) { it.toByte() }
        fileHandler.writeBytes("binary.dat", binaryData)

        // Export
        val zipBytes = fileHandler.zipFiles()
        zipBytes.shouldNotBeEmpty()

        // Clear and import
        fileHandler.deleteFilesDirectory()
        val inputStream = zipBytes.toInputStream()
        fileHandler.unzipAndReplaceFiles(inputStream)

        // Verify binary data
        val imported = fileHandler.readBytes("binary.dat")
        imported shouldBe binaryData

        // Cleanup
        fileHandler.deleteFilesDirectory()

        println("✓ Binary data ZIP test completed!")
    }
})

// Platform-specific FileHandler creation
expect fun createPlatformFileHandler(): FileHandler

// Helper to convert ByteArray to platform-specific InputStream
expect fun ByteArray.toInputStream(): Any
