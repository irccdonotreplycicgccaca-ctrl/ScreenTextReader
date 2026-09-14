package com.krmob.screentextreader.feature.capture.data

import android.graphics.Bitmap
import android.media.Image
import androidx.core.graphics.createBitmap
import java.nio.ByteBuffer

/**
 * Copies RGBA_8888 images into bitmaps. Devices may pad every row except the last one, so the plane is first
 * copied into a buffer sized for full rows and the padding columns are cropped off afterwards.
 */
internal class RgbaFrameCopier {
    private var rowAlignedBuffer: ByteBuffer? = null

    fun copy(image: Image): Bitmap {
        val plane = image.planes.first()
        val paddedWidth = plane.rowStride / plane.pixelStride
        val buffer = rowAlignedBuffer(capacity = plane.rowStride * image.height)
        buffer.put(plane.buffer)
        buffer.rewind()
        val paddedBitmap = createBitmap(paddedWidth, image.height).apply { copyPixelsFromBuffer(buffer) }
        return if (paddedWidth == image.width) {
            paddedBitmap
        } else {
            Bitmap.createBitmap(paddedBitmap, 0, 0, image.width, image.height)
        }
    }

    private fun rowAlignedBuffer(capacity: Int): ByteBuffer {
        val buffer =
            rowAlignedBuffer?.takeIf { existing -> existing.capacity() == capacity }
                ?: ByteBuffer.allocateDirect(capacity).also { allocated -> rowAlignedBuffer = allocated }
        buffer.clear()
        return buffer
    }
}
