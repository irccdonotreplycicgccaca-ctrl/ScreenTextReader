
package com.krmob.screentextreader.feature.analysis.domain

import java.math.BigDecimal
import javax.inject.Inject

/**
 * Extracts Crash multipliers written as X1.74, X1,74, etc.
 * Searches every recognized OCR line.
 */
class CrashMultiplierParser @Inject constructor() {

    private val pattern = Regex(
        """(?i)(?<![\p{L}\p{N}])X\s*(\d+(?:[.,]\d+)?)(?![\p{L}\p{N}]|[.,]\d)"""
    )

    fun parse(lines: List<String>): List<BigDecimal> = lines.flatMap { line ->
        pattern.findAll(line).map { match ->
            BigDecimal(match.groupValues[1].replace(',', '.'))
        }.toList()
    }
}
