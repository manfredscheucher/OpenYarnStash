package org.example.project

actual fun sendEmail(address: String, subject: String) {
    // Not supported on wasm-js
}
