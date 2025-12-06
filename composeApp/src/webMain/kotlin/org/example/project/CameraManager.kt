package org.example.project

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import org.khronos.webgl.Uint8ClampedArray
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLVideoElement
import org.w3c.dom.Image
import org.w3c.dom.ImageData
import org.w3c.dom.mediacapture.MediaStream
import org.w3c.dom.mediacapture.MediaStreamConstraints
import kotlin.js.Promise

@Composable
actual fun rememberCameraLauncher(onResult: (ByteArray?) -> Unit): CameraLauncher? {
    return remember { CameraLauncher(onResult) }
}

actual class CameraLauncher(private val onResult: (ByteArray?) -> Unit) {
    private var videoElement: HTMLVideoElement? = null
    private var stream: MediaStream? = null
    private var overlayDiv: HTMLDivElement? = null

    actual fun launch() {
        GlobalScope.launch {
            try {
                // Create video element for camera preview
                val video = document.createElement("video") as HTMLVideoElement
                video.setAttribute("autoplay", "true")
                video.setAttribute("playsinline", "true")
                video.style.width = "100%"
                video.style.maxWidth = "640px"

                // Create overlay div for camera UI
                val overlay = document.createElement("div") as HTMLDivElement
                overlay.style.position = "fixed"
                overlay.style.top = "0"
                overlay.style.left = "0"
                overlay.style.width = "100%"
                overlay.style.height = "100%"
                overlay.style.backgroundColor = "rgba(0, 0, 0, 0.9)"
                overlay.style.display = "flex"
                overlay.style.flexDirection = "column"
                overlay.style.alignItems = "center"
                overlay.style.justifyContent = "center"
                overlay.style.zIndex = "10000"

                // Create capture button
                val captureButton = document.createElement("button") as org.w3c.dom.HTMLButtonElement
                captureButton.textContent = "📷 Capture"
                captureButton.style.marginTop = "20px"
                captureButton.style.padding = "15px 30px"
                captureButton.style.fontSize = "18px"
                captureButton.style.cursor = "pointer"

                // Create cancel button
                val cancelButton = document.createElement("button") as org.w3c.dom.HTMLButtonElement
                cancelButton.textContent = "✕ Cancel"
                cancelButton.style.marginTop = "10px"
                cancelButton.style.padding = "15px 30px"
                cancelButton.style.fontSize = "18px"
                cancelButton.style.cursor = "pointer"

                overlay.appendChild(video)
                overlay.appendChild(captureButton)
                overlay.appendChild(cancelButton)
                document.body?.appendChild(overlay)

                videoElement = video
                overlayDiv = overlay

                // Get camera stream
                val constraints = js("({video: {facingMode: 'environment'}, audio: false})")
                val mediaStream = window.navigator.mediaDevices.getUserMedia(constraints.unsafeCast<MediaStreamConstraints>()).await()
                stream = mediaStream
                video.srcObject = mediaStream

                // Capture button click handler
                captureButton.onclick = {
                    captureImage()
                    null
                }

                // Cancel button click handler
                cancelButton.onclick = {
                    cleanup()
                    onResult(null)
                    null
                }

            } catch (e: Exception) {
                console.error("Error accessing camera: ${e.message}")
                cleanup()
                onResult(null)
            }
        }
    }

    private fun captureImage() {
        val video = videoElement ?: return

        try {
            // Create canvas to capture the image
            val canvas = document.createElement("canvas") as HTMLCanvasElement
            canvas.width = video.videoWidth
            canvas.height = video.videoHeight

            val context = canvas.getContext("2d") as org.w3c.dom.CanvasRenderingContext2D
            context.drawImage(video, 0.0, 0.0)

            // Convert canvas to blob
            canvas.toBlob({ blob ->
                if (blob != null) {
                    val reader = org.w3c.files.FileReader()
                    reader.onload = {
                        val arrayBuffer = reader.result as org.khronos.webgl.ArrayBuffer
                        val int8Array = org.khronos.webgl.Int8Array(arrayBuffer)
                        val byteArray = ByteArray(int8Array.length) { i ->
                            int8Array.asDynamic()[i] as Byte
                        }
                        cleanup()
                        onResult(byteArray)
                        null
                    }
                    reader.readAsArrayBuffer(blob)
                } else {
                    cleanup()
                    onResult(null)
                }
            }, "image/jpeg", 0.9)

        } catch (e: Exception) {
            console.error("Error capturing image: ${e.message}")
            cleanup()
            onResult(null)
        }
    }

    private fun cleanup() {
        // Stop camera stream
        stream?.getTracks()?.forEach { track ->
            track.stop()
        }
        stream = null

        // Remove overlay
        overlayDiv?.let { document.body?.removeChild(it) }
        overlayDiv = null
        videoElement = null
    }
}
