package com.krmob.screentextreader.feature.capture.data

import com.krmob.screentextreader.core.model.RecognizedText
import com.krmob.screentextreader.feature.capture.domain.CaptureFailure
import com.krmob.screentextreader.feature.capture.domain.CaptureSession
import com.krmob.screentextreader.feature.capture.domain.CaptureState
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Single source of truth for the capture session: the service writes to it, the UI observes it. */
@Singleton
class CaptureSessionStore @Inject constructor() : CaptureSession {
    private val mutableState = MutableStateFlow<CaptureState>(CaptureState.Idle)
    override val state: StateFlow<CaptureState> = mutableState.asStateFlow()

    private val mutableRecognizedText = MutableStateFlow(RecognizedText.EMPTY)
    override val recognizedText: StateFlow<RecognizedText> = mutableRecognizedText.asStateFlow()

    fun markStarting() {
        mutableRecognizedText.value = RecognizedText.EMPTY
        mutableState.value = CaptureState.Starting
    }

    fun markRunning(isPaused: Boolean) {
        mutableState.value = CaptureState.Running(isPaused = isPaused)
    }

    fun publish(recognizedText: RecognizedText) {
        mutableRecognizedText.value = recognizedText
    }

    /** Returns to idle, unless the session ended with a failure the user has not dismissed yet. */
    fun markStopped() {
        mutableState.update { current -> if (current is CaptureState.Failed) current else CaptureState.Idle }
    }

    fun markFailed(failure: CaptureFailure) {
        mutableState.value = CaptureState.Failed(failure)
    }

    fun dismissFailure() {
        mutableState.update { current -> if (current is CaptureState.Failed) CaptureState.Idle else current }
    }
}
