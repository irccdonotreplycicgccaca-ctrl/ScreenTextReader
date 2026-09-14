package com.krmob.screentextreader.feature.capture.ui

import android.content.Context
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.krmob.screentextreader.R
import com.krmob.screentextreader.core.designsystem.theme.ScreenTextReaderTheme
import com.krmob.screentextreader.core.model.RecognizedLine
import com.krmob.screentextreader.feature.analysis.domain.AnalysisSettings
import com.krmob.screentextreader.feature.analysis.domain.DecimalSeparator
import com.krmob.screentextreader.feature.analysis.domain.NumberStatistics
import com.krmob.screentextreader.feature.capture.domain.CaptureFailure
import com.krmob.screentextreader.feature.capture.domain.CaptureState
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CaptureScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `idle screen starts capture when start is clicked`() {
        var startClickCount = 0
        setScreen(CaptureUiState.INITIAL, onStartClick = { startClickCount++ })

        composeTestRule.onNodeWithText(context.getString(R.string.action_start_capture)).performClick()

        composeTestRule.runOnIdle { assertEquals(1, startClickCount) }
    }

    @Test
    fun `running screen stops capture when stop is clicked`() {
        var stopClickCount = 0
        setScreen(
            CaptureUiState.INITIAL.copy(captureState = CaptureState.Running(isPaused = false)),
            onStopClick = { stopClickCount++ }
        )

        composeTestRule.onNodeWithText(context.getString(R.string.action_stop_capture)).performClick()

        composeTestRule.runOnIdle { assertEquals(1, stopClickCount) }
    }

    @Test
    fun `failed screen explains the failure and lets the user dismiss it`() {
        var dismissClickCount = 0
        setScreen(
            CaptureUiState.INITIAL.copy(captureState = CaptureState.Failed(CaptureFailure.CONSENT_DENIED)),
            onFailureDismiss = { dismissClickCount++ }
        )

        composeTestRule.onNodeWithText(context.getString(R.string.failure_consent_denied)).assertExists()
        composeTestRule.onNodeWithText(context.getString(R.string.action_dismiss)).performClick()

        composeTestRule.runOnIdle { assertEquals(1, dismissClickCount) }
    }

    @Test
    fun `shows statistics with the selected decimal separator and the recognized lines`() {
        val line = RecognizedLine(text = "Faktura FV 214/09/2026", confidence = 0.97f)
        setScreen(
            CaptureUiState.INITIAL.copy(
                settings = AnalysisSettings.DEFAULT.copy(decimalSeparator = DecimalSeparator.COMMA),
                acceptedLines = persistentListOf(line),
                statistics =
                    NumberStatistics.Summary(
                        count = 2,
                        sum = BigDecimal("1537.50"),
                        minimum = BigDecimal("287.50"),
                        maximum = BigDecimal("1250.00"),
                        average = BigDecimal("768.75")
                    )
            )
        )

        val list = composeTestRule.onNode(hasScrollAction())
        list.performScrollToNode(hasText("1537,50"))
        list.performScrollToNode(hasText(line.text))
    }

    private fun setScreen(
        uiState: CaptureUiState,
        onStartClick: () -> Unit = {},
        onStopClick: () -> Unit = {},
        onFailureDismiss: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            ScreenTextReaderTheme {
                CaptureScreen(
                    uiState = uiState,
                    onStartClick = onStartClick,
                    onStopClick = onStopClick,
                    onFailureDismiss = onFailureDismiss,
                    onMinimumConfidenceChange = {},
                    onDecimalSeparatorChange = {}
                )
            }
        }
    }
}
