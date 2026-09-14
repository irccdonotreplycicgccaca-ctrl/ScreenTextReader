package com.krmob.screentextreader.feature.analysis.domain

import com.krmob.screentextreader.core.model.RecognizedLine
import com.krmob.screentextreader.core.model.RecognizedText
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertIs
import org.junit.Test

class TextAnalyzerTest {
    private val analyzer = TextAnalyzer(NumberParser())

    @Test
    fun `accepts lines at or above the minimum confidence and counts the rest`() {
        val certain = line("Suma 10,00", confidence = 0.9f)
        val borderline = line("Rabat 2,00", confidence = 0.6f)
        val uncertain = line("Szum 99", confidence = 0.59f)

        val analysis = analyzer.analyze(textOf(certain, borderline, uncertain), settings(minimumConfidence = 0.6f))

        assertEquals(listOf(certain, borderline), analysis.acceptedLines)
        assertEquals(1, analysis.rejectedLineCount)
    }

    @Test
    fun `summarises numbers from accepted lines only`() {
        val text = textOf(line("Pozycja 1: 12,50"), line("Pozycja 2: 7,50"), line("Szum 1000", confidence = 0.1f))

        val statistics = assertIs<NumberStatistics.Summary>(analyzer.analyze(text, settings()).statistics)

        assertEquals(4, statistics.count)
        assertEquals(BigDecimal("23.00"), statistics.sum)
        assertEquals(BigDecimal("1"), statistics.minimum)
        assertEquals(BigDecimal("12.50"), statistics.maximum)
        assertEquals(BigDecimal("5.75"), statistics.average)
    }

    @Test
    fun `rounds the average half up to two decimal places`() {
        val text = textOf(line("1 2 2"))

        val statistics = assertIs<NumberStatistics.Summary>(analyzer.analyze(text, settings()).statistics)

        assertEquals(BigDecimal("1.67"), statistics.average)
    }

    @Test
    fun `parses numbers with the selected decimal separator`() {
        val text = textOf(line("Kwota 1.234"))

        val commaStatistics =
            assertIs<NumberStatistics.Summary>(
                analyzer.analyze(text, settings(decimalSeparator = DecimalSeparator.COMMA)).statistics
            )
        val dotStatistics =
            assertIs<NumberStatistics.Summary>(
                analyzer.analyze(text, settings(decimalSeparator = DecimalSeparator.DOT)).statistics
            )

        assertEquals(BigDecimal("1234"), commaStatistics.sum)
        assertEquals(BigDecimal("1.234"), dotStatistics.sum)
    }

    @Test
    fun `reports no statistics when accepted lines contain no numbers`() {
        val analysis = analyzer.analyze(textOf(line("Brak liczb")), settings())

        assertEquals(NumberStatistics.None, analysis.statistics)
    }

    private fun line(text: String, confidence: Float = 0.9f) = RecognizedLine(text = text, confidence = confidence)

    private fun textOf(vararg lines: RecognizedLine) = RecognizedText(lines = lines.toList())

    private fun settings(minimumConfidence: Float = 0.5f, decimalSeparator: DecimalSeparator = DecimalSeparator.COMMA) =
        AnalysisSettings(minimumConfidence = minimumConfidence, decimalSeparator = decimalSeparator)
}
