package org.example.project

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.InputStreamReader

private const val CREDENTIALS_PATH = "/credentials.json"
private val JSON_FACTORY = JacksonFactory.getDefaultInstance()
private val TOKENS_DIRECTORY_PATH = java.io.File("tokens")
private val SCOPES = listOf(DriveScopes.DRIVE_FILE)

/**
 * Manages Google Drive interactions, including authentication and file operations for JVM.
 */
actual class GoogleDriveManager {
    private var credential by mutableStateOf<Credential?>(null)
    private var driveService by mutableStateOf<Drive?>(null)

    actual val isSignedIn: Boolean
        get() = credential != null

    @Composable
    actual fun signIn(onResult: (Boolean) -> Unit) {
        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                try {
                    val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
                    val clientSecrets = GoogleClientSecrets.load(
                        JSON_FACTORY,
                        InputStreamReader(GoogleDriveManager::class.java.getResourceAsStream(CREDENTIALS_PATH)
                            ?: throw FileNotFoundException("Resource not found: $CREDENTIALS_PATH"))
                    )

                    val flow = GoogleAuthorizationCodeFlow.Builder(
                        httpTransport, JSON_FACTORY, clientSecrets, SCOPES
                    )
                        .setDataStoreFactory(FileDataStoreFactory(TOKENS_DIRECTORY_PATH))
                        .setAccessType("offline")
                        .build()

                    val receiver = LocalServerReceiver.Builder().setPort(8888).build()
                    val newCredential = AuthorizationCodeInstalledApp(flow, receiver).authorize("user")

                    credential = newCredential
                    driveService = Drive.Builder(httpTransport, JSON_FACTORY, newCredential)
                        .setApplicationName("OpenYarnStash")
                        .build()

                    onResult(true)
                } catch (e: Exception) {
                    e.printStackTrace()
                    onResult(false)
                }
            }
        }
    }

    actual fun signOut() {
        // For this simple implementation, signing out means deleting the token directory.
        // A more robust implementation might use the API to revoke the token.
        TOKENS_DIRECTORY_PATH.deleteRecursively()
        credential = null
        driveService = null
    }

    actual suspend fun uploadFile(fileName: String, content: String, onResult: (Boolean) -> Unit) {
        withContext(Dispatchers.IO) {
            val service = driveService ?: return@withContext onResult(false)
            try {
                val fileId = findFileId(service, fileName)
                val metadata = File().setName(fileName)
                val contentStream = ByteArrayContent.fromString("application/json", content)

                if (fileId != null) {
                    service.files().update(fileId, metadata, contentStream).execute()
                } else {
                    service.files().create(metadata, contentStream).execute()
                }
                onResult(true)
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(false)
            }
        }
    }

    actual suspend fun downloadFile(fileName: String, onResult: (String?) -> Unit) {
        withContext(Dispatchers.IO) {
            val service = driveService ?: return@withContext onResult(null)
            try {
                val fileId = findFileId(service, fileName)
                if (fileId == null) {
                    onResult(null) // File not found
                    return@withContext
                }

                val outputStream = ByteArrayOutputStream()
                service.files().get(fileId).executeMediaAndDownloadTo(outputStream)
                val fileContent = outputStream.toString(Charsets.UTF_8.name())
                onResult(fileContent)
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(null)
            }
        }
    }

    private fun findFileId(service: Drive, name: String): String? {
        var pageToken: String? = null
        do {
            val result = service.files().list()
                .setQ("name = '$name' and trashed = false")
                .setSpaces("drive")
                .setFields("nextPageToken, files(id, name)")
                .setPageToken(pageToken)
                .execute()

            result.files.firstOrNull()?.let {
                return it.id
            }
            pageToken = result.nextPageToken
        } while (pageToken != null)
        return null
    }
}

@Composable
actual fun rememberGoogleDriveManager(): GoogleDriveManager {
    return remember { GoogleDriveManager() }
}
