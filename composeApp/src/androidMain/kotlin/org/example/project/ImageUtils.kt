package org.example.project

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream

actual fun createEmptyImageByteArray(): ByteArray {
    val bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    return stream.toByteArray()
}
