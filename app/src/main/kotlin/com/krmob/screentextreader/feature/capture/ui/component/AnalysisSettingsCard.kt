package com.krmob.screentextreader.feature.capture.ui.component

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.krmob.screentextreader.R
import com.krmob.screentextreader.core.designsystem.theme.Dimens
import com.krmob.screentextreader.feature.analysis.domain.AnalysisSettings
import com.krmob.screentextreader.feature.analysis.domain.DecimalSeparator
import com.krmob.screentextreader.feature.capture.ui.toPercent

// Nineteen intermediate stops give five percent steps between zero and one hundred percent.
private const val CONFIDENCE_SLIDER_STEPS = 19

@Composable
internal fun AnalysisSettingsCard(
    settings: AnalysisSettings,
    onMinimumConfidenceChange: (Float) -> Unit,
    onDecimalSeparatorChange: (DecimalSeparator) -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(Dimens.spacingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall)
        ) {
            Text(text = stringResource(R.string.settings_title), style = MaterialTheme.typography.titleMedium)

            val confidenceLabel =
                stringResource(R.string.settings_minimum_confidence, settings.minimumConfidence.toPercent())
            Text(text = confidenceLabel, style = MaterialTheme.typography.bodyMedium)
            Slider(
                value = settings.minimumConfidence,
                onValueChange = onMinimumConfidenceChange,
                valueRange = AnalysisSettings.CONFIDENCE_RANGE,
                steps = CONFIDENCE_SLIDER_STEPS,
                modifier = Modifier.semantics { contentDescription = confidenceLabel }
            )

            Text(
                text = stringResource(R.string.settings_decimal_separator),
                style = MaterialTheme.typography.bodyMedium
            )
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                DecimalSeparator.entries.forEachIndexed { index, decimalSeparator ->
                    SegmentedButton(
                        selected = decimalSeparator == settings.decimalSeparator,
                        onClick = { onDecimalSeparatorChange(decimalSeparator) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = DecimalSeparator.entries.size)
                    ) {
                        Text(text = stringResource(decimalSeparator.labelRes()))
                    }
                }
            }
        }
    }
}

@StringRes
private fun DecimalSeparator.labelRes(): Int = when (this) {
    DecimalSeparator.COMMA -> R.string.decimal_separator_comma
    DecimalSeparator.DOT -> R.string.decimal_separator_dot
}
