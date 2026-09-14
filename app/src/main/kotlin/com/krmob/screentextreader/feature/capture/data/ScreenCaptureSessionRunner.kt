package com.krmob.screentextreader.feature.capture.data

import android.content.Context
import android.graphics.Bitmap
import android.media.projection.MediaProjection
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.window.layout.WindowMetricsCalculator
import com.google.mlkit.common.MlKitException
import com.krmob.screentextreader.core.coroutines.DefaultDispatcher
import com.krmob.screentextreader.core.lifecycle.AppVisibility
import com.krmob.screentextreader.core.model.RecognizedText
import com.krmob.screentextreader.feature.capture.domain.ScreenTextReader
import com.krmob.screentextreader.feature.capture.domain.scan
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

private const val TAG = "ScreenCaptureSession"
private val SCAN_INTERVAL = 1.seconds

/** Runs one capture session: mirrors the screen, recognizes text on new frames and publishes it to the store. */
class ScreenCaptureSessionRunner @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val sessionStore: CaptureSessionStore,
    private val appVisibility: AppVisibility,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {
    /** Suspends until the projection ends or the caller is cancelled; the projection is always stopped on return. */
    suspend fun run(mediaProjection: MediaProjection) {
        val projectionCallback = ProjectionCallback(initialSize = maximumWindowSize())
        // The callback must be registered before the virtual display is created.
        mediaProjection.registerCallback(projectionCallback, Handler(Looper.getMainLooper()))
        try {
            MlKitTextRecognizer().use { recognizer ->
                MediaProjectionScreenCapturer(
                    mediaProjection = mediaProjection,
                    contentSize = projectionCallback.contentSize,
                    densityDpi = context.resources.configuration.densityDpi
                ).use { capturer ->
                    val reader = ScreenTextReader {
                        capturer.latestFrame()?.let { frame -> recognizer.recognizeOrSkip(frame) }
                    }
                    // Reading this app's own screen would feed the results back into the recognizer.
                    val shouldScan = combine(
                        projectionCallback.isContentVisible,
                        appVisibility.isInForeground
                    ) { isContentVisible, isAppInForeground -> isContentVisible && !isAppInForeground }
                    Log.i(TAG, "Capture session started")
                    coroutineScope {
                        launch { shouldScan.collect { isScanning -> sessionStore.markRunning(isPaused = !isScanning) } }
                        launch {
                            reader
                                .scan(shouldScan, SCAN_INTERVAL)
                                .flowOn(defaultDispatcher)
                                .collect(sessionStore::publish)
                        }
                        projectionCallback.awaitStop()
                        coroutineContext.cancelChildren()
                    }
                }
            }
        } finally {
            mediaProjection.unregisterCallback(projectionCallback)
            mediaProjection.stop()
            sessionStore.markStopped()
            Log.i(TAG, "Capture session stopped")
        }
    }

    private fun maximumWindowSize(): CaptureSize {
        val bounds = WindowMetricsCalculator.getOrCreate().computeMaximumWindowMetrics(context).bounds
        return CaptureSize(width = bounds.width(), height = bounds.height())
    }
}

private suspend fun MlKitTextRecognizer.recognizeOrSkip(frame: Bitmap): RecognizedText? = try {
    recognize(frame)
} catch (exception: MlKitException) {
    Log.w(TAG, "Text recognition failed, skipping the frame", exception)
    null
}
