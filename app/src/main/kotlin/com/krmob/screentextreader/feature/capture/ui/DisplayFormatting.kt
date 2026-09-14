package com.krmob.screentextreader.feature.capture.ui

import com.krmob.screentextreader.feature.analysis.domain.DecimalSeparator
import java.math.BigDecimal
import kotlin.math.roundToInt

private const val PERCENT_SCALE = 100

internal fun Float.toPercent(): Int = (this * PERCENT_SCALE).roundToInt()

/** Formats a number with the same decimal separator the user expects on the captured screen. */
internal fun BigDecimal.toDisplayString(decimalSeparator: DecimalSeparator): String =
    toPlainString().replace('.', decimalSeparator.symbol)
