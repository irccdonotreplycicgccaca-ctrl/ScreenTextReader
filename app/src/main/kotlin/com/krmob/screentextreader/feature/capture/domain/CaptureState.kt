package com.krmob.screentextreader.feature.capture.domain

sealed interface CaptureState {
    data object Idle : CaptureState

    data object Starting : CaptureState

    /** Capture is active; [isPaused] is true while the captured content is hidden or this app is in front. */
    data class Running(val isPaused: Boolean) : CaptureState

    data class Failed(val failure: CaptureFailure) : CaptureState
}

enum class CaptureFailure {
    CONSENT_DENIED,
    PROJECTION_UNAVAILABLE,
    CAPTURE_ERROR
}
