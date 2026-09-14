package com.krmob.screentextreader.feature.capture.service

import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.projection.MediaProjectionManager
import android.util.Log
import androidx.core.app.ServiceCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.krmob.screentextreader.feature.capture.data.CaptureSessionStore
import com.krmob.screentextreader.feature.capture.data.ScreenCaptureSessionRunner
import com.krmob.screentextreader.feature.capture.domain.CaptureFailure
import com.krmob.screentextreader.feature.capture.domain.CaptureState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

private const val TAG = "ScreenCaptureService"
private const val ACTION_START_CAPTURE = "com.krmob.screentextreader.action.START_CAPTURE"
private const val ACTION_STOP_CAPTURE = "com.krmob.screentextreader.action.STOP_CAPTURE"

/** Foreground service that owns the media projection for the lifetime of one capture session. */
@AndroidEntryPoint
class ScreenCaptureService : LifecycleService() {
    @Inject lateinit var sessionRunner: ScreenCaptureSessionRunner

    @Inject lateinit var sessionStore: CaptureSessionStore

    @Inject lateinit var notifications: CaptureNotifications

    private var captureJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        notifications.createChannel()
        lifecycleScope.launch {
            combine(sessionStore.state, sessionStore.recognizedText) { state, recognizedText ->
                state to recognizedText.lines.size
            }.collect { (state, lineCount) ->
                if (state is CaptureState.Running) {
                    notifications.update(isPaused = state.isPaused, lineCount = lineCount)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_START_CAPTURE -> startCapture(intent)
            ACTION_STOP_CAPTURE -> stopSelf()
            else -> Log.w(TAG, "Ignoring unknown command ${intent?.action}")
        }
        return START_NOT_STICKY
    }

    private fun startCapture(intent: Intent) {
        // startForegroundService() obliges the service to enter the foreground even when the session cannot start.
        ServiceCompat.startForeground(
            this,
            CaptureNotifications.NOTIFICATION_ID,
            notifications.build(isPaused = false, lineCount = 0),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
        )
        if (captureJob?.isActive == true) {
            Log.w(TAG, "Capture is already running, ignoring another start command")
            return
        }
        val consent = CaptureConsent.readFrom(intent)
        if (consent == null) {
            Log.e(TAG, "Start command arrived without the projection consent")
            sessionStore.markFailed(CaptureFailure.PROJECTION_UNAVAILABLE)
            stopSelf()
            return
        }
        captureJob = lifecycleScope.launch { capture(consent) }
    }

    private suspend fun capture(consent: CaptureConsent) {
        try {
            val mediaProjectionManager =
                checkNotNull(getSystemService<MediaProjectionManager>()) { "MediaProjectionManager is not available" }
            val mediaProjection = mediaProjectionManager.getMediaProjection(consent.resultCode, consent.data)
            if (mediaProjection == null) {
                Log.e(TAG, "The system returned no media projection for the granted consent")
                sessionStore.markFailed(CaptureFailure.PROJECTION_UNAVAILABLE)
                return
            }
            sessionRunner.run(mediaProjection)
        } catch (exception: CancellationException) {
            Log.d(TAG, "Capture cancelled with the service", exception)
            throw exception
        } catch (exception: SecurityException) {
            Log.e(TAG, "The system rejected the media projection", exception)
            sessionStore.markFailed(CaptureFailure.PROJECTION_UNAVAILABLE)
        } catch (exception: IllegalStateException) {
            Log.e(TAG, "Screen capture failed", exception)
            sessionStore.markFailed(CaptureFailure.CAPTURE_ERROR)
        } finally {
            stopSelf()
        }
    }

    companion object {
        internal fun startIntent(context: Context, consent: CaptureConsent): Intent =
            consent.writeTo(Intent(context, ScreenCaptureService::class.java).setAction(ACTION_START_CAPTURE))

        internal fun stopIntent(context: Context): Intent =
            Intent(context, ScreenCaptureService::class.java).setAction(ACTION_STOP_CAPTURE)
    }
}
