package com.krmob.screentextreader.feature.capture.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.krmob.screentextreader.core.permissions.checkNotificationPermission

@Composable
fun CaptureRoute(modifier: Modifier = Modifier, viewModel: CaptureViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val consentLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            viewModel.onConsentResult(result.resultCode, result.data)
        }
    // Notifications only report progress, so capture continues whether or not the permission is granted.
    val notificationPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            consentLauncher.launch(viewModel.createConsentIntent())
        }

    CaptureScreen(
        uiState = uiState,
        onStartClick = {
            if (context.checkNotificationPermission()) {
                consentLauncher.launch(viewModel.createConsentIntent())
            } else {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        },
        onStopClick = viewModel::onStopClick,
        onFailureDismiss = viewModel::onFailureDismiss,
        onMinimumConfidenceChange = viewModel::onMinimumConfidenceChange,
        onDecimalSeparatorChange = viewModel::onDecimalSeparatorChange,
        modifier = modifier
    )
}
