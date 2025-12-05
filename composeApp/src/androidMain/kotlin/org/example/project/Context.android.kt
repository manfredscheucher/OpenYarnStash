package org.example.project

import android.content.Context

private lateinit var applicationContext: Context

fun setContext(context: Context) {
    applicationContext = context
}

actual fun getContext(): Any? = applicationContext
