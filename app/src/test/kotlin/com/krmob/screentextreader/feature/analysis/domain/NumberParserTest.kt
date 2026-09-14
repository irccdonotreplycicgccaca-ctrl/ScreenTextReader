package com.krmob.screentextreader.feature.analysis.domain

import java.math.BigDecimal
import kotlin.test.assertEquals
import org.junit.Test

class NumberParserTest {
    private val parser = NumberParser()

    @Test
    fun `reads grouped and plain numbers written with a decimal comma`() {
        val numbers = parser.parse("Netto 1 234,56 zł, VAT 12 i rabat 1.000,5", DecimalSeparator.COMMA)

        assertEquals(numbers("1234.56", "12", "1000.5"), numbers)
    }

    @Test
    fun `reads grouped and plain numbers written with a decimal dot`() {
        val numbers = parser.parse("Total 1,234.50 of 7 items", DecimalSeparator.DOT)

        assertEquals(numbers("1234.50", "7"), numbers)
    }

    @Test
    fun `treats non-breaking spaces as digit grouping`() {
        val numbers = parser.parse("Saldo 12\u00A0345,00", DecimalSeparator.COMMA)

        assertEquals(numbers("12345.00"), numbers)
    }

    @Test
    fun `reads negative numbers with a hyphen or a minus sign`() {
        val numbers = parser.parse("Zmiana -12,5 oraz \u22123", DecimalSeparator.COMMA)

        assertEquals(numbers("-12.5", "-3"), numbers)
    }

    @Test
    fun `reads the same digits differently depending on the decimal separator`() {
        assertEquals(numbers("1234"), parser.parse("1.234", DecimalSeparator.COMMA))
        assertEquals(numbers("1.234"), parser.parse("1.234", DecimalSeparator.DOT))
    }

    @Test
    fun `skips a number written with the other decimal separator instead of splitting it`() {
        assertEquals(emptyList(), parser.parse("Cena 12.5", DecimalSeparator.COMMA))
        assertEquals(emptyList(), parser.parse("Price 12,50", DecimalSeparator.DOT))
    }

    @Test
    fun `skips digits glued to letters`() {
        val numbers = parser.parse("Model A4 X200 kosztuje 15", DecimalSeparator.COMMA)

        assertEquals(numbers("15"), numbers)
    }

    @Test
    fun `reads both ends of a range and a number before sentence punctuation`() {
        val numbers = parser.parse("Godziny 5-10. Razem: 42.", DecimalSeparator.COMMA)

        assertEquals(numbers("5", "10", "42"), numbers)
    }

    @Test
    fun `returns nothing for text without numbers`() {
        assertEquals(emptyList(), parser.parse("Brak liczb w tej linii", DecimalSeparator.COMMA))
    }

    private fun numbers(vararg values: String): List<BigDecimal> = values.map(::BigDecimal)
}
