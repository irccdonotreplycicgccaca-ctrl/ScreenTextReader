package com.krmob.screentextreader.feature.capture.data

/** Pixel size of the captured content. */
internal data class CaptureSize(val width: Int, val height: Int) {
    init {
        require(width > 0 && height > 0) { "Capture size must be positive, was ${width}x$height" }
    }
}
