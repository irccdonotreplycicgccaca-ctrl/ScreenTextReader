package com.krmob.screentextreader.feature.capture.ui.component

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.krmob.screentextreader.R
import com.krmob.screentextreader.core.designsystem.theme.Dimens
import com.krmob.screentextreader.feature.capture.domain.CaptureFailure
import com.krmob.screentextreader.feature.capture.domain.CaptureState

@Composable
internal fun CaptureStatusCard(
    captureState: CaptureState,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit,
    onFailureDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(Dimens.spacingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall)
        ) {
            Text(text = stringResource(captureState.titleRes()), style = MaterialTheme.typography.titleMedium)
            Text(
                text = stringResource(captureState.descriptionRes()),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (captureState == CaptureState.Starting) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            when (captureState) {
                CaptureState.Idle -> {
                    Button(onClick = onStartClick) { Text(text = stringResource(R.string.action_start_capture)) }
                }

                CaptureState.Starting, is CaptureState.Running -> {
                    OutlinedButton(onClick = onStopClick) { Text(text = stringResource(R.string.action_stop_capture)) }
                }

                is CaptureState.Failed -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSmall)) {
                        Button(onClick = onStartClick) { Text(text = stringResource(R.string.action_try_again)) }
                        TextButton(onClick = onFailureDismiss) { Text(text = stringResource(R.string.action_dismiss)) }
                    }
                }
            }
        }
    }
}

@StringRes
private fun CaptureState.titleRes(): Int = when (this) {
    CaptureState.Idle -> R.string.status_idle_title
    CaptureState.Starting -> R.string.status_starting_title
    is CaptureState.Running -> if (isPaused) R.string.status_paused_title else R.string.status_reading_title
    is CaptureState.Failed -> R.string.status_failed_title
}

@StringRes
private fun CaptureState.descriptionRes(): Int = when (this) {
    CaptureState.Idle -> R.string.status_idle_description
    CaptureState.Starting -> R.string.status_starting_description
    is CaptureState.Running -> if (isPaused) R.string.status_paused_description else R.string.status_reading_description
    is CaptureState.Failed -> failure.messageRes()
}

@StringRes
private fun CaptureFailure.messageRes(): Int = when (this) {
    CaptureFailure.CONSENT_DENIED -> R.string.failure_consent_denied
    CaptureFailure.PROJECTION_UNAVAILABLE -> R.string.failure_projection_unavailable
    CaptureFailure.CAPTURE_ERROR -> R.string.failure_capture_error
}
