package com.krmob.screentextreader.feature.analysis.domain

/** Parameters of the analysis applied to every recognized frame. */
data class AnalysisSettings(val minimumConfidence: Float, val decimalSeparator: DecimalSeparator) {
    init {
        require(minimumConfidence in CONFIDENCE_RANGE) {
            "Minimum confidence must be within $CONFIDENCE_RANGE, was $minimumConfidence"
        }
    }

    companion object {
        val CONFIDENCE_RANGE = 0f..1f
        private const val DEFAULT_MINIMUM_CONFIDENCE = 0.5f

        val DEFAULT =
            AnalysisSettings(
                minimumConfidence = DEFAULT_MINIMUM_CONFIDENCE,
                decimalSeparator = DecimalSeparator.COMMA
            )
    }
}
