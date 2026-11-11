
package com.krmob.screentextreader.feature.capture.ui

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.krmob.screentextreader.core.model.RecognizedLine
import com.krmob.screentextreader.feature.analysis.domain.NumberStatistics
import com.krmob.screentextreader.feature.capture.domain.CaptureFailure
import com.krmob.screentextreader.feature.capture.domain.CaptureState
import java.math.BigDecimal
import kotlinx.collections.immutable.persistentListOf

internal class CaptureUiStatePreviewParameterProvider : PreviewParameterProvider<CaptureUiState> {
    private val readingState =
        CaptureUiState.INITIAL.copy(
            captureState = CaptureState.Running(isPaused = false),
            acceptedLines =
                persistentListOf(
                    RecognizedLine(text = "X1.74", confidence = 0.97f),
                    RecognizedLine(text = "X1.17", confidence = 0.93f),
                    RecognizedLine(text = "X2.10", confidence = 0.91f)
                ),
            rejectedLineCount = 1,
            statistics =
                NumberStatistics.Summary(
                    count = 2,
                    sum = BigDecimal("3.27"),
                    minimum = BigDecimal("1.17"),
                    maximum = BigDecimal("2.10"),
                    average = BigDecimal("1.635")
                ),
            crashMultipliers = persistentListOf(
                BigDecimal("1.74"),
                BigDecimal("1.17"),
                BigDecimal("2.10")
            )
        )

    override val values: Sequence<CaptureUiState> =
        sequenceOf(
            CaptureUiState.INITIAL,
            readingState,
            readingState.copy(captureState = CaptureState.Running(isPaused = true)),
            CaptureUiState.INITIAL.copy(captureState = CaptureState.Failed(CaptureFailure.CONSENT_DENIED))
        )
}
