package org.example.project

import androidx.compose.runtime.Composable

/**
 * Manages Google Drive interactions, including authentication and file operations.
 */
expect class GoogleDriveManager {

    /**
     * Indicates whether the user is currently signed in.
     */
    val isSignedIn: Boolean

    /**
     * Initiates the sign-in process.
     */
    @Composable
    fun signIn(onResult: (Boolean) -> Unit)

    /**
     * Signs the user out.
     */
    fun signOut()

    /**
     * Uploads a file to Google Drive.
     *
     * @param fileName The name of the file to be saved on Drive.
     * @param content The content of the file.
     * @param onResult Callback with true on success, false otherwise.
     */
    suspend fun uploadFile(fileName: String, content: String, onResult: (Boolean) -> Unit)

    /**
     * Downloads a file from Google Drive.
     *
     * @param fileName The name of the file to download.
     * @param onResult Callback with the file content on success, or null on failure.
     */
    suspend fun downloadFile(fileName: String, onResult: (String?) -> Unit)
}

/**
 * A composable that provides an instance of [GoogleDriveManager].
 */
@Composable
expect fun rememberGoogleDriveManager(): GoogleDriveManager
