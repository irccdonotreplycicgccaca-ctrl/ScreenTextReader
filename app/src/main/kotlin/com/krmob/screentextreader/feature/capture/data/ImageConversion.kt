package com.krmob.screentextreader.feature.capture.data

import android.graphics.Bitmap
import android.media.Image
import androidx.core.graphics.createBitmap
import java.nio.ByteBuffer

/**
 * Copies an RGBA_8888 image into a bitmap. Devices may pad each row of the plane, and the plane ends after the
 * last pixel of the last row, so the bytes are first laid out in full rows and the padding columns cropped off.
 */
internal fun Image.toBitmap(): Bitmap {
    val plane = planes.first()
    val paddedWidth = plane.rowStride / plane.pixelStride
    val rowAlignedBytes = ByteArray(plane.rowStride * height)
    plane.buffer.get(rowAlignedBytes, 0, plane.buffer.remaining())
    val paddedBitmap = createBitmap(paddedWidth, height).apply {
        copyPixelsFromBuffer(ByteBuffer.wrap(rowAlignedBytes))
    }
    return if (paddedWidth == width) paddedBitmap else Bitmap.createBitmap(paddedBitmap, 0, 0, width, height)
}
