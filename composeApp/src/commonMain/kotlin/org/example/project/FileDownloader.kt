package org.example.project

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * A platform-specific implementation for downloading or sharing a file.
 */
interface FileDownloader {
    fun download(fileName: String, data: String)
}

/**
 * CompositionLocal to provide the [FileDownloader] implementation.
 */
val LocalFileDownloader = staticCompositionLocalOf<FileDownloader> {
    error("No FileDownloader provided")
}
