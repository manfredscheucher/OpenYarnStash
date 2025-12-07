package org.example.project

import kotlinx.browser.document
import org.khronos.webgl.Int8Array
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

actual fun createEmptyImageByteArray(): ByteArray {
    // Create a 1x1 pixel transparent PNG
    val canvas = document.createElement("canvas") as HTMLCanvasElement
    canvas.width = 1
    canvas.height = 1
    val ctx = canvas.getContext("2d") as CanvasRenderingContext2D

    // Create a minimal PNG byte array (1x1 transparent pixel)
    // PNG header + IHDR + IDAT + IEND chunks
    return byteArrayOf(
        0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, // PNG signature
        0x00, 0x00, 0x00, 0x0D, // IHDR length
        0x49, 0x48, 0x44, 0x52, // IHDR
        0x00, 0x00, 0x00, 0x01, // width: 1
        0x00, 0x00, 0x00, 0x01, // height: 1
        0x08, 0x06, 0x00, 0x00, 0x00, // bit depth, color type, compression, filter, interlace
        0x1F, 0x15, 0xC4.toByte(), 0x89.toByte(), // CRC
        0x00, 0x00, 0x00, 0x0A, // IDAT length
        0x49, 0x44, 0x41, 0x54, // IDAT
        0x08, 0xD7.toByte(), 0x63, 0x00, 0x00, 0x00, 0x02, 0x00, 0x01, // data
        0xE2.toByte(), 0x21, 0xBC.toByte(), 0x33, // CRC
        0x00, 0x00, 0x00, 0x00, // IEND length
        0x49, 0x45, 0x4E, 0x44, // IEND
        0xAE.toByte(), 0x42, 0x60, 0x82.toByte() // CRC
    )
}
