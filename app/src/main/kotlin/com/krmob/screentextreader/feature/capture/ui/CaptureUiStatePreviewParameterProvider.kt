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
                    RecognizedLine(text = "Faktura FV 214/09/2026", confidence = 0.97f),
                    RecognizedLine(text = "Netto 1 250,00 zł", confidence = 0.93f),
                    RecognizedLine(text = "VAT 287,50 zł", confidence = 0.91f)
                ),
            rejectedLineCount = 1,
            statistics =
                NumberStatistics.Summary(
                    count = 2,
                    sum = BigDecimal("1537.50"),
                    minimum = BigDecimal("287.50"),
                    maximum = BigDecimal("1250.00"),
                    average = BigDecimal("768.75")
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
