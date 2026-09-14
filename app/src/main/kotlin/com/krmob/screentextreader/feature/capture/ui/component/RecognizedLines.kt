package com.krmob.screentextreader.feature.capture.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.krmob.screentextreader.R
import com.krmob.screentextreader.core.designsystem.theme.Dimens
import com.krmob.screentextreader.core.model.RecognizedLine
import com.krmob.screentextreader.feature.capture.ui.toPercent

private const val LINE_TEXT_WEIGHT = 1f

@Composable
internal fun RecognizedLinesHeader(rejectedLineCount: Int, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.lines_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.semantics { heading() }
        )
        if (rejectedLineCount > 0) {
            Text(
                text = pluralStringResource(R.plurals.lines_rejected, rejectedLineCount, rejectedLineCount),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
internal fun RecognizedLineRow(line: RecognizedLine, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMedium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = line.text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(LINE_TEXT_WEIGHT)
        )
        Text(
            text = stringResource(R.string.line_confidence, line.confidence.toPercent()),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
