package com.krmob.screentextreader.feature.analysis.domain

import java.math.BigDecimal
import java.math.RoundingMode

/** Summary of the numbers found in the accepted lines of one frame. */
sealed interface NumberStatistics {
    data object None : NumberStatistics

    data class Summary(
        val count: Int,
        val sum: BigDecimal,
        val minimum: BigDecimal,
        val maximum: BigDecimal,
        val average: BigDecimal
    ) : NumberStatistics

    companion object {
        private const val AVERAGE_SCALE = 2

        fun of(numbers: List<BigDecimal>): NumberStatistics {
            if (numbers.isEmpty()) return None
            val sum = numbers.fold(BigDecimal.ZERO, BigDecimal::add)
            return Summary(
                count = numbers.size,
                sum = sum,
                minimum = numbers.min(),
                maximum = numbers.max(),
                average = sum.divide(numbers.size.toBigDecimal(), AVERAGE_SCALE, RoundingMode.HALF_UP)
            )
        }
    }
}
