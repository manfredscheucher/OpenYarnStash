package org.example.project

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Placeholder implementation for GoogleDriveManager on Android.
 * This version is not functional and serves to allow the app to compile.
 */
actual class GoogleDriveManager {
    actual val isSignedIn: Boolean
        get() = false

    @Composable
    actual fun signIn(onResult: (Boolean) -> Unit) {
        // Not implemented on Android yet. Report failure immediately.
        Log.w("GoogleDriveManager", "Sign-in is not implemented on Android.")
        onResult(false)
    }

    actual fun signOut() {
        // Not implemented
        Log.w("GoogleDriveManager", "Sign-out is not implemented on Android.")
    }

    actual suspend fun uploadFile(fileName: String, content: String, onResult: (Boolean) -> Unit) {
        // Not implemented
        Log.w("GoogleDriveManager", "File upload is not implemented on Android.")
        onResult(false)
    }

    actual suspend fun downloadFile(fileName: String, onResult: (String?) -> Unit) {
        // Not implemented
        Log.w("GoogleDriveManager", "File download is not implemented on Android.")
        onResult(null)
    }
}

@Composable
actual fun rememberGoogleDriveManager(): GoogleDriveManager {
    return remember { GoogleDriveManager() }
}
