package com.krmob.screentextreader.feature.capture.ui.component

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.krmob.screentextreader.R
import com.krmob.screentextreader.core.designsystem.theme.Dimens
import com.krmob.screentextreader.feature.analysis.domain.DecimalSeparator
import com.krmob.screentextreader.feature.analysis.domain.NumberStatistics
import com.krmob.screentextreader.feature.capture.ui.toDisplayString

@Composable
internal fun NumberStatisticsCard(
    statistics: NumberStatistics,
    decimalSeparator: DecimalSeparator,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(Dimens.spacingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall)
        ) {
            Text(text = stringResource(R.string.statistics_title), style = MaterialTheme.typography.titleMedium)
            when (statistics) {
                NumberStatistics.None -> {
                    Text(
                        text = stringResource(R.string.statistics_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                is NumberStatistics.Summary -> {
                    StatisticRow(label = R.string.statistics_count, value = statistics.count.toString())
                    StatisticRow(
                        label = R.string.statistics_sum,
                        value = statistics.sum.toDisplayString(decimalSeparator)
                    )
                    StatisticRow(
                        label = R.string.statistics_minimum,
                        value = statistics.minimum.toDisplayString(decimalSeparator)
                    )
                    StatisticRow(
                        label = R.string.statistics_maximum,
                        value = statistics.maximum.toDisplayString(decimalSeparator)
                    )
                    StatisticRow(
                        label = R.string.statistics_average,
                        value = statistics.average.toDisplayString(decimalSeparator)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatisticRow(@StringRes label: Int, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = stringResource(label), style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.titleSmall)
    }
}
