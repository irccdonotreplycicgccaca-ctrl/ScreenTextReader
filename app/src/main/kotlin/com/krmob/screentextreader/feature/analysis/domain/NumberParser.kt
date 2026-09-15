package com.krmob.screentextreader.feature.analysis.domain

import java.math.BigDecimal
import javax.inject.Inject

private const val MINUS_SIGN = '\u2212'

/** Finds the numbers written in a line of recognized text. */
class NumberParser @Inject constructor() {
    private val patterns: Map<DecimalSeparator, Regex> =
        DecimalSeparator.entries.associateWith { decimalSeparator -> numberPattern(decimalSeparator) }

    fun parse(text: String, decimalSeparator: DecimalSeparator): List<BigDecimal> = patterns
        .getValue(decimalSeparator)
        .findAll(text)
        .map { match -> match.value.parseNumber(decimalSeparator) }
        .toList()
}

/**
 * Matches a number that stands on its own, grouped (`1 234,56`) or plain (`1234,56`), with an optional sign.
 * A token glued to letters or digits, or written with the other decimal separator, is rejected as ambiguous
 * instead of being read as a different value.
 */
private fun numberPattern(decimalSeparator: DecimalSeparator): Regex {
    val decimal = decimalSeparator.symbol.toRegexLiteral()
    val grouping = decimalSeparator.groupingSymbols.joinToString(separator = "") { symbol -> symbol.toRegexLiteral() }
    val anySeparator = "[.,]"
    return Regex(
        "(?<![\\p{L}\\p{N}]|\\p{N}$anySeparator)" +
            "[-$MINUS_SIGN]?" +
            "(?:\\d{1,3}(?:[$grouping]\\d{3})+|\\d+)" +
            "(?:$decimal\\d+)?" +
            "(?![\\p{L}\\p{N}]|$anySeparator\\p{N})"
    )
}

// A backslash before any non-letter keeps separators such as '.' and spaces literal inside a character class.
private fun Char.toRegexLiteral(): String = "\\$this"

private fun String.parseNumber(decimalSeparator: DecimalSeparator): BigDecimal {
    val normalized =
        buildString {
            for (character in this@parseNumber) {
                when (character) {
                    in decimalSeparator.groupingSymbols -> Unit
                    decimalSeparator.symbol -> append('.')
                    MINUS_SIGN -> append('-')
                    else -> append(character)
                }
            }
        }
    return BigDecimal(normalized)
}
