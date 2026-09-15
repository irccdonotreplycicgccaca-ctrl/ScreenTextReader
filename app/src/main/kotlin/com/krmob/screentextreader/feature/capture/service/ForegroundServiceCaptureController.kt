package com.krmob.screentextreader.feature.capture.service

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.krmob.screentextreader.feature.capture.data.CaptureSessionStore
import com.krmob.screentextreader.feature.capture.domain.CaptureFailure
import com.krmob.screentextreader.feature.capture.domain.ScreenCaptureController
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private const val TAG = "CaptureController"

/** Hands the user's consent to [ScreenCaptureService], which owns the projection while it runs. */
class ForegroundServiceCaptureController @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val mediaProjectionManager: MediaProjectionManager,
    private val sessionStore: CaptureSessionStore
) : ScreenCaptureController {
    override fun createConsentIntent(): Intent = mediaProjectionManager.createScreenCaptureIntent()

    override fun onConsentResult(resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK || data == null) {
            Log.i(TAG, "The user did not allow screen capture")
            sessionStore.markFailed(CaptureFailure.CONSENT_DENIED)
            return
        }
        sessionStore.markStarting()
        try {
            val consent = CaptureConsent(resultCode = resultCode, data = data)
            ContextCompat.startForegroundService(context, ScreenCaptureService.startIntent(context, consent))
        } catch (exception: IllegalStateException) {
            Log.e(TAG, "The capture service could not be started", exception)
            sessionStore.markFailed(CaptureFailure.CAPTURE_ERROR)
        }
    }

    override fun stop() {
        context.stopService(Intent(context, ScreenCaptureService::class.java))
    }

    override fun dismissFailure() {
        sessionStore.dismissFailure()
    }
}
