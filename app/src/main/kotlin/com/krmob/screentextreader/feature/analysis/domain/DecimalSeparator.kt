package com.krmob.screentextreader.feature.analysis.domain

private const val NO_BREAK_SPACE = '\u00A0'
private const val NARROW_NO_BREAK_SPACE = '\u202F'
private val SPACE_SYMBOLS = setOf(' ', NO_BREAK_SPACE, NARROW_NO_BREAK_SPACE)

/** Decides whether `1.234,56` or `1,234.56` is the expected way of writing numbers on the captured screen. */
enum class DecimalSeparator(val symbol: Char, val groupingSymbols: Set<Char>) {
    COMMA(symbol = ',', groupingSymbols = SPACE_SYMBOLS + '.'),
    DOT(symbol = '.', groupingSymbols = SPACE_SYMBOLS + ',')
}
