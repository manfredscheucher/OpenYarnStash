package org.example.project

import androidx.compose.runtime.compositionLocalOf

class DesktopBackDispatcher {
    private val handlers = mutableListOf<() -> Unit>()

    fun register(handler: () -> Unit) {
        handlers.add(handler)
    }

    fun unregister(handler: () -> Unit) {
        handlers.remove(handler)
    }

    fun dispatch(): Boolean {
        if (handlers.isNotEmpty()) {
            handlers.last().invoke()
            return true
        }
        return false
    }
}

val LocalDesktopBackDispatcher = compositionLocalOf<DesktopBackDispatcher> { error("No Back Dispatcher provided") }
