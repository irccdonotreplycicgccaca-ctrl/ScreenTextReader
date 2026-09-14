package com.krmob.screentextreader.feature.capture.domain

import com.krmob.screentextreader.core.model.RecognizedText
import kotlinx.coroutines.flow.StateFlow

/** Observable state of the screen capture session, shared by the capture service and the UI. */
interface CaptureSession {
    val state: StateFlow<CaptureState>

    /** The latest recognized text, kept after the session stops so the last result stays readable. */
    val recognizedText: StateFlow<RecognizedText>
}
