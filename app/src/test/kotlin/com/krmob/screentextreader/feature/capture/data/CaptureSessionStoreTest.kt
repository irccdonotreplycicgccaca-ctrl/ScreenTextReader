package com.krmob.screentextreader.feature.capture.data

import com.krmob.screentextreader.core.model.RecognizedLine
import com.krmob.screentextreader.core.model.RecognizedText
import com.krmob.screentextreader.feature.capture.domain.CaptureFailure
import com.krmob.screentextreader.feature.capture.domain.CaptureState
import kotlin.test.assertEquals
import org.junit.Test

class CaptureSessionStoreTest {
    private val store = CaptureSessionStore()

    @Test
    fun `starting a session clears the text of the previous one`() {
        store.publish(RecognizedText(lines = listOf(RecognizedLine(text = "Stary tekst", confidence = 0.9f))))

        store.markStarting()

        assertEquals(CaptureState.Starting, store.state.value)
        assertEquals(RecognizedText.EMPTY, store.recognizedText.value)
    }

    @Test
    fun `stopping a running session returns to idle and keeps the last text`() {
        val lastText = RecognizedText(lines = listOf(RecognizedLine(text = "Ostatni odczyt", confidence = 0.9f)))
        store.markRunning(isPaused = false)
        store.publish(lastText)

        store.markStopped()

        assertEquals(CaptureState.Idle, store.state.value)
        assertEquals(lastText, store.recognizedText.value)
    }

    @Test
    fun `stopping after a failure keeps the failure visible`() {
        store.markFailed(CaptureFailure.CAPTURE_ERROR)

        store.markStopped()

        assertEquals(CaptureState.Failed(CaptureFailure.CAPTURE_ERROR), store.state.value)
    }

    @Test
    fun `dismissing clears a failure but never interrupts a running session`() {
        store.markFailed(CaptureFailure.CONSENT_DENIED)
        store.dismissFailure()
        assertEquals(CaptureState.Idle, store.state.value)

        store.markRunning(isPaused = true)
        store.dismissFailure()
        assertEquals(CaptureState.Running(isPaused = true), store.state.value)
    }
}
