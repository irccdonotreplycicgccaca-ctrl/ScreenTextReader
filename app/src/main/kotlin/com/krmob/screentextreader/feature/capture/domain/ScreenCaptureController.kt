package com.krmob.screentextreader.feature.capture.domain

import android.content.Intent

/** Starts and stops screen capture on behalf of the UI. */
interface ScreenCaptureController {
    /** Intent for the system dialog in which the user consents to a single capture session. */
    fun createConsentIntent(): Intent

    fun onConsentResult(resultCode: Int, data: Intent?)

    fun stop()

    fun dismissFailure()
}
