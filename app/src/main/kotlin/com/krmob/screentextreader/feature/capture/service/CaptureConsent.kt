package com.krmob.screentextreader.feature.capture.service

import android.app.Activity
import android.content.Intent
import androidx.core.content.IntentCompat

private const val EXTRA_RESULT_CODE = "com.krmob.screentextreader.extra.RESULT_CODE"
private const val EXTRA_RESULT_DATA = "com.krmob.screentextreader.extra.RESULT_DATA"

/** Result of the system consent dialog; it grants exactly one media projection. */
internal class CaptureConsent(val resultCode: Int, val data: Intent) {
    fun writeTo(intent: Intent): Intent = intent
        .putExtra(EXTRA_RESULT_CODE, resultCode)
        .putExtra(EXTRA_RESULT_DATA, data)

    companion object {
        fun readFrom(intent: Intent): CaptureConsent? {
            val data = IntentCompat.getParcelableExtra(intent, EXTRA_RESULT_DATA, Intent::class.java) ?: return null
            return CaptureConsent(
                resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, Activity.RESULT_CANCELED),
                data = data
            )
        }
    }
}
