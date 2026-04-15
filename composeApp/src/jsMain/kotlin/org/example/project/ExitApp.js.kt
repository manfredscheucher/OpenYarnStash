package org.example.project

actual fun exitApp() {
    // No-op for web: closing the tab is not possible from JS without user gesture
}
