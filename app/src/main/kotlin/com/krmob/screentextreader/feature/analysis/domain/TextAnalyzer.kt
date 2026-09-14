package com.krmob.screentextreader.feature.analysis.domain

import com.krmob.screentextreader.core.model.RecognizedText
import javax.inject.Inject

/** Drops lines the recognizer is unsure about and summarises the numbers in the remaining ones. */
class TextAnalyzer @Inject constructor(private val numberParser: NumberParser) {
    fun analyze(recognizedText: RecognizedText, settings: AnalysisSettings): TextAnalysis {
        val (acceptedLines, rejectedLines) =
            recognizedText.lines.partition { line -> line.confidence >= settings.minimumConfidence }
        val numbers = acceptedLines.flatMap { line -> numberParser.parse(line.text, settings.decimalSeparator) }
        return TextAnalysis(
            acceptedLines = acceptedLines,
            rejectedLineCount = rejectedLines.size,
            statistics = NumberStatistics.of(numbers)
        )
    }
}
