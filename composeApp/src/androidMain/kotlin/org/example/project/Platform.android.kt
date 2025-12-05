package org.example.project

import android.content.Context
import android.os.Build

class AndroidPlatform(private val context: Context) : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
    override val fileHandler: FileHandler = AndroidFileHandler(context)
}

actual fun getPlatform(context: Any?): Platform = AndroidPlatform(context as Context)
