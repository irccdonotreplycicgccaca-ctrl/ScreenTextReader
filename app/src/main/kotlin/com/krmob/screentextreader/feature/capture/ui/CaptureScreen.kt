package com.krmob.screentextreader.feature.capture.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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

private const val HEADER_KEY = "header"
private const val STATUS_KEY = "status"
private const val SETTINGS_KEY = "settings"
private const val STATISTICS_KEY = "statistics"
private const val LINES_HEADER_KEY = "linesHeader"
private const val LINES_EMPTY_KEY = "linesEmpty"
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
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .consumeWindowInsets(contentPadding),
            contentPadding = PaddingValues(all = Dimens.spacingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingMedium)
        ) {
            item(key = HEADER_KEY) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.semantics { heading() }
                )
            }
            item(key = STATUS_KEY) {
                CaptureStatusCard(
                    captureState = uiState.captureState,
                    onStartClick = onStartClick,
                    onStopClick = onStopClick,
                    onFailureDismiss = onFailureDismiss
                )
            }
            item(key = SETTINGS_KEY) {
                AnalysisSettingsCard(
                    settings = uiState.settings,
                    onMinimumConfidenceChange = onMinimumConfidenceChange,
                    onDecimalSeparatorChange = onDecimalSeparatorChange
                )
            }
            item(key = STATISTICS_KEY) {
                NumberStatisticsCard(
                    statistics = uiState.statistics,
                    decimalSeparator = uiState.settings.decimalSeparator
                )
            }
            item(key = LINES_HEADER_KEY) {
                RecognizedLinesHeader(rejectedLineCount = uiState.rejectedLineCount)
            }
            if (uiState.acceptedLines.isEmpty()) {
                item(key = LINES_EMPTY_KEY) {
                    Text(
                        text = stringResource(R.string.lines_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                itemsIndexed(
                    items = uiState.acceptedLines,
                    key = { index, _ -> index },
                    contentType = { _, _ -> LINE_CONTENT_TYPE }
                ) { _, line ->
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
