package org.example.project

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import kotlinx.coroutines.launch

@Composable
actual fun rememberCameraLauncher(onResult: (ByteArray?) -> Unit): CameraLauncher? {
    val context = LocalContext.current
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            photoUri?.let { uri ->
                scope.launch {
                    val imageBytes = getRotatedImageBytes(context, uri)
                    onResult(imageBytes)
                }
            }
        } else {
            onResult(null)
        }
    }

    return remember(launcher) {
        object : CameraLauncher {
            override fun launch() {
                try {
                    val file = createImageFile(context)
                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                    photoUri = uri
                    launcher.launch(uri)
                } catch (e: IOException) {
                    scope.launch {
                        Logger.log(LogLevel.ERROR, "Failed to create image file", e)
                    }
                    onResult(null)
                }
            }
        }
    }
}

private suspend fun getRotatedImageBytes(context: Context, photoUri: Uri): ByteArray? {
    return try {
        val inputStream = context.contentResolver.openInputStream(photoUri) ?: return null
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        val exifInputStream = context.contentResolver.openInputStream(photoUri) ?: return null
        val exifInterface = ExifInterface(exifInputStream)
        val orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        exifInputStream.close()

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1.0f, 1.0f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.preScale(1.0f, -1.0f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.postRotate(90f)
                matrix.preScale(-1.0f, 1.0f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.postRotate(-90f)
                matrix.preScale(-1.0f, 1.0f)
            }
        }

        val rotatedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)
        val stream = ByteArrayOutputStream()
        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        stream.toByteArray()

    } catch (e: IOException) {
        Logger.log(LogLevel.ERROR, "Failed to get rotated image bytes", e)
        e.printStackTrace()
        null
    }
}


private fun createImageFile(context: Context): File {
    val timeStamp = System.currentTimeMillis()
    val storageDir: File? = context.getExternalFilesDir(null)
    return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
}
