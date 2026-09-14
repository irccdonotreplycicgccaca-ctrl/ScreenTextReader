package com.krmob.screentextreader.feature.capture.ui

import com.krmob.screentextreader.feature.analysis.domain.DecimalSeparator
import java.math.BigDecimal
import kotlin.test.assertEquals
import org.junit.Test

class DisplayFormattingTest {
    @Test
    fun `formats numbers with the selected decimal separator without exponent notation`() {
        assertEquals("1234,50", BigDecimal("1234.50").toDisplayString(DecimalSeparator.COMMA))
        assertEquals("-0.5", BigDecimal("-0.5").toDisplayString(DecimalSeparator.DOT))
        assertEquals("1000000", BigDecimal("1E+6").toDisplayString(DecimalSeparator.COMMA))
    }

    @Test
    fun `rounds confidence to whole percent`() {
        assertEquals(0, 0f.toPercent())
        assertEquals(87, 0.874f.toPercent())
        assertEquals(100, 1f.toPercent())
    }
}
