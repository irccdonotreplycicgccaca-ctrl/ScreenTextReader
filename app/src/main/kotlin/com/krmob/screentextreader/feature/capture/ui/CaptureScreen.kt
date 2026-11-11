
package com.krmob.screentextreader.feature.capture.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.krmob.screentextreader.R
import com.krmob.screentextreader.core.designsystem.theme.Dimens
import com.krmob.screentextreader.core.designsystem.theme.ScreenTextReaderTheme
import com.krmob.screentextreader.feature.analysis.domain.DecimalSeparator
import com.krmob.screentextreader.feature.capture.ui.component.AnalysisSettingsCard
import com.krmob.screentextreader.feature.capture.ui.component.CaptureStatusCard
import com.krmob.screentextreader.feature.capture.ui.component.NumberStatisticsCard
import com.krmob.screentextreader.feature.capture.ui.component.RecognizedLineRow
import com.krmob.screentextreader.feature.capture.ui.component.RecognizedLinesHeader

private const val LINE_CONTENT_TYPE = "line"

@Composable
fun CaptureScreen(
    uiState: CaptureUiState,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit,
    onFailureDismiss: () -> Unit,
    onMinimumConfidenceChange: (Float) -> Unit,
    onDecimalSeparatorChange: (DecimalSeparator) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(modifier = modifier) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .consumeWindowInsets(contentPadding),
            contentPadding = PaddingValues(all = Dimens.spacingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingMedium)
        ) {
            item {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.semantics { heading() }
                )
            }

            item {
                CaptureStatusCard(
                    captureState = uiState.captureState,
                    onStartClick = onStartClick,
                    onStopClick = onStopClick,
                    onFailureDismiss = onFailureDismiss
                )
            }

            item {
                Text(
                    text = "Multiplicateurs détectés",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = if (uiState.crashMultipliers.isEmpty()) {
                        "Aucun multiplicateur détecté"
                    } else {
                        uiState.crashMultipliers.joinToString("   ") {
                            "X${it.stripTrailingZeros().toPlainString()}"
                        }
                    },
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            item {
                AnalysisSettingsCard(
                    settings = uiState.settings,
                    onMinimumConfidenceChange = onMinimumConfidenceChange,
                    onDecimalSeparatorChange = onDecimalSeparatorChange
                )
            }

            item {
                NumberStatisticsCard(
                    statistics = uiState.statistics,
                    decimalSeparator = uiState.settings.decimalSeparator
                )
            }

            item {
                RecognizedLinesHeader(rejectedLineCount = uiState.rejectedLineCount)
            }

            if (uiState.acceptedLines.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.lines_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(uiState.acceptedLines, contentType = { LINE_CONTENT_TYPE }) { line ->
                    RecognizedLineRow(line = line)
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun CaptureScreenPreview(
    @PreviewParameter(CaptureUiStatePreviewParameterProvider::class) uiState: CaptureUiState
) {
    ScreenTextReaderTheme {
        CaptureScreen(
            uiState = uiState,
            onStartClick = {},
            onStopClick = {},
            onFailureDismiss = {},
            onMinimumConfidenceChange = {},
            onDecimalSeparatorChange = {}
        )
    }
}
