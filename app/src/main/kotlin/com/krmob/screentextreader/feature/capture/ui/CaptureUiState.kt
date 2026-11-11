
package com.krmob.screentextreader.feature.capture.ui

import com.krmob.screentextreader.core.model.RecognizedLine
import com.krmob.screentextreader.feature.analysis.domain.AnalysisSettings
import com.krmob.screentextreader.feature.analysis.domain.NumberStatistics
import com.krmob.screentextreader.feature.capture.domain.CaptureState
import java.math.BigDecimal
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class CaptureUiState(
    val captureState: CaptureState,
    val settings: AnalysisSettings,
    val acceptedLines: ImmutableList<RecognizedLine>,
    val rejectedLineCount: Int,
    val statistics: NumberStatistics,
    val crashMultipliers: ImmutableList<BigDecimal>
) {
    companion object {
        val INITIAL =
            CaptureUiState(
                captureState = CaptureState.Idle,
                settings = AnalysisSettings.DEFAULT,
                acceptedLines = persistentListOf(),
                rejectedLineCount = 0,
                statistics = NumberStatistics.None,
                crashMultipliers = persistentListOf()
            )
    }
}
