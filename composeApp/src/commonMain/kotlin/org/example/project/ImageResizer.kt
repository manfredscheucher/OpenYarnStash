package org.example.project

expect suspend fun resizeImage(bytes: ByteArray, maxWidth: Int, maxHeight: Int): ByteArray
