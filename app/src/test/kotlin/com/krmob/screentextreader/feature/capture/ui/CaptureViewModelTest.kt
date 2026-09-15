package com.krmob.screentextreader.feature.capture.ui

import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.krmob.screentextreader.core.model.RecognizedLine
import com.krmob.screentextreader.core.model.RecognizedText
import com.krmob.screentextreader.feature.analysis.domain.DecimalSeparator
import com.krmob.screentextreader.feature.analysis.domain.NumberParser
import com.krmob.screentextreader.feature.analysis.domain.NumberStatistics
import com.krmob.screentextreader.feature.analysis.domain.TextAnalyzer
import com.krmob.screentextreader.feature.capture.domain.CaptureSession
import com.krmob.screentextreader.feature.capture.domain.CaptureState
import com.krmob.screentextreader.feature.capture.domain.ScreenCaptureController
import com.krmob.screentextreader.testing.MainDispatcherRule
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

private const val DECLINED_RESULT_CODE = 0

class CaptureViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val captureSession = FakeCaptureSession()
    private val captureController = FakeScreenCaptureController()

    @Test
    fun `mirrors the state of the capture session`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(CaptureState.Idle, awaitItem().captureState)

            captureSession.state.value = CaptureState.Running(isPaused = true)

            assertEquals(CaptureState.Running(isPaused = true), awaitItem().captureState)
        }
    }

    @Test
    fun `analyses recognized text with the current settings`() = runTest {
        val total = RecognizedLine(text = "Do zapłaty 10,50 zł", confidence = 0.9f)
        val noise = RecognizedLine(text = "Szum 99", confidence = 0.2f)
        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(1)
            captureSession.recognizedText.value = RecognizedText(lines = listOf(total, noise))

            val uiState = awaitItem()
            assertEquals(listOf(total), uiState.acceptedLines)
            assertEquals(1, uiState.rejectedLineCount)
            assertEquals(BigDecimal("10.50"), summaryOf(uiState).sum)
        }
    }

    @Test
    fun `raising the minimum confidence re-analyses the text already recognized`() = runTest {
        captureSession.recognizedText.value = textOf(RecognizedLine(text = "Suma 5", confidence = 0.8f))
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(1, awaitItem().acceptedLines.size)

            viewModel.onMinimumConfidenceChange(0.9f)

            val uiState = awaitItem()
            assertEquals(0.9f, uiState.settings.minimumConfidence)
            assertEquals(emptyList(), uiState.acceptedLines)
            assertEquals(NumberStatistics.None, uiState.statistics)
        }
    }

    @Test
    fun `switching the decimal separator re-parses the numbers`() = runTest {
        captureSession.recognizedText.value = textOf(RecognizedLine(text = "Kwota 1.234", confidence = 0.9f))
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(BigDecimal("1234"), summaryOf(awaitItem()).sum)

            viewModel.onDecimalSeparatorChange(DecimalSeparator.DOT)

            assertEquals(BigDecimal("1.234"), summaryOf(awaitItem()).sum)
        }
    }

    @Test
    fun `keeps the minimum confidence within the valid range`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(1)
            viewModel.onMinimumConfidenceChange(1.5f)

            assertEquals(1f, awaitItem().settings.minimumConfidence)
        }
    }

    @Test
    fun `restores the analysis settings after the view model is recreated`() = runTest {
        val savedStateHandle = SavedStateHandle()
        createViewModel(savedStateHandle).onDecimalSeparatorChange(DecimalSeparator.DOT)

        createViewModel(savedStateHandle).uiState.test {
            assertEquals(DecimalSeparator.DOT, awaitItem().settings.decimalSeparator)
        }
    }

    @Test
    fun `forwards user intents to the capture controller`() {
        val viewModel = createViewModel()

        viewModel.onConsentResult(DECLINED_RESULT_CODE, null)
        viewModel.onStopClick()
        viewModel.onFailureDismiss()

        assertEquals(listOf(DECLINED_RESULT_CODE), captureController.consentResultCodes)
        assertEquals(1, captureController.stopCount)
        assertEquals(1, captureController.dismissCount)
    }

    private fun createViewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()) = CaptureViewModel(
        captureSession = captureSession,
        captureController = captureController,
        textAnalyzer = TextAnalyzer(NumberParser()),
        savedStateHandle = savedStateHandle,
        defaultDispatcher = mainDispatcherRule.testDispatcher
    )

    private fun textOf(vararg lines: RecognizedLine) = RecognizedText(lines = lines.toList())

    private fun summaryOf(uiState: CaptureUiState) = assertIs<NumberStatistics.Summary>(uiState.statistics)
}

private class FakeCaptureSession : CaptureSession {
    override val state = MutableStateFlow<CaptureState>(CaptureState.Idle)
    override val recognizedText = MutableStateFlow(RecognizedText.EMPTY)
}

private class FakeScreenCaptureController : ScreenCaptureController {
    val consentResultCodes = mutableListOf<Int>()

    var stopCount = 0
        private set

    var dismissCount = 0
        private set

    override fun createConsentIntent(): Intent = error("The consent intent is created by the Android framework")

    override fun onConsentResult(resultCode: Int, data: Intent?) {
        consentResultCodes += resultCode
    }

    override fun stop() {
        stopCount++
    }

    override fun dismissFailure() {
        dismissCount++
    }
}
