package com.krmob.screentextreader.feature.analysis.domain

import com.krmob.screentextreader.core.model.RecognizedLine

/** Result of analysing one recognized frame with the current [AnalysisSettings]. */
data class TextAnalysis(
    val acceptedLines: List<RecognizedLine>,
    val rejectedLineCount: Int,
    val statistics: NumberStatistics
)
