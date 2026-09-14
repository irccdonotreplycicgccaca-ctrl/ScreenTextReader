package com.krmob.screentextreader.feature.capture.data

import android.media.projection.MediaProjection
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Exposes [MediaProjection.Callback] events as state that the capture loop can read from any thread. */
internal class ProjectionCallback(initialSize: CaptureSize) : MediaProjection.Callback() {
    private val mutableContentSize = MutableStateFlow(initialSize)
    val contentSize: StateFlow<CaptureSize> = mutableContentSize.asStateFlow()

    private val mutableIsContentVisible = MutableStateFlow(true)
    val isContentVisible: StateFlow<Boolean> = mutableIsContentVisible.asStateFlow()

    private val stopped = CompletableDeferred<Unit>()

    /** Suspends until the user or the system ends the projection, for example from the status bar or by locking. */
    suspend fun awaitStop() {
        stopped.await()
    }

    override fun onStop() {
        stopped.complete(Unit)
    }

    override fun onCapturedContentResize(width: Int, height: Int) {
        if (width > 0 && height > 0) {
            mutableContentSize.value = CaptureSize(width = width, height = height)
        }
    }

    override fun onCapturedContentVisibilityChanged(isVisible: Boolean) {
        mutableIsContentVisible.value = isVisible
    }
}
