package com.krmob.screentextreader.feature.capture.data

import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.media.ImageReader
import android.media.projection.MediaProjection
import kotlinx.coroutines.flow.StateFlow

private const val VIRTUAL_DISPLAY_NAME = "ScreenTextReader"

// acquireLatestImage() can only skip stale frames when the reader may hold one more image than it hands out.
private const val MAX_BUFFERED_IMAGES = 2

/**
 * Mirrors the projected content into an [ImageReader] and copies the newest frame on request.
 * Not thread-safe: all calls must come from one coroutine, one at a time.
 */
internal class MediaProjectionScreenCapturer(
    mediaProjection: MediaProjection,
    private val contentSize: StateFlow<CaptureSize>,
    private val densityDpi: Int
) : AutoCloseable {
    private var currentSize = contentSize.value
    private var imageReader = newImageReader(currentSize)
    private val virtualDisplay =
        checkNotNull(
            mediaProjection.createVirtualDisplay(
                VIRTUAL_DISPLAY_NAME,
                currentSize.width,
                currentSize.height,
                densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader.surface,
                null,
                null
            )
        ) { "The virtual display for screen capture could not be created" }
    private val frameCopier = RgbaFrameCopier()

    /** Returns the newest frame, or null when the screen has not changed since the previous call. */
    fun latestFrame(): Bitmap? {
        resizeIfContentSizeChanged()
        return imageReader.acquireLatestImage()?.use(frameCopier::copy)
    }

    override fun close() {
        virtualDisplay.release()
        imageReader.close()
    }

    private fun resizeIfContentSizeChanged() {
        val targetSize = contentSize.value
        if (targetSize == currentSize) return
        val previousReader = imageReader
        imageReader = newImageReader(targetSize)
        virtualDisplay.resize(targetSize.width, targetSize.height, densityDpi)
        virtualDisplay.surface = imageReader.surface
        previousReader.close()
        currentSize = targetSize
    }
}

private fun newImageReader(size: CaptureSize): ImageReader =
    ImageReader.newInstance(size.width, size.height, PixelFormat.RGBA_8888, MAX_BUFFERED_IMAGES)
