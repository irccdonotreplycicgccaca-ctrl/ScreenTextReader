package com.krmob.screentextreader.feature.capture.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

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
            if (shouldRequestNotificationPermission(context)) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                consentLauncher.launch(viewModel.createConsentIntent())
            }
        },
        onStopClick = viewModel::onStopClick,
        onFailureDismiss = viewModel::onFailureDismiss,
        onMinimumConfidenceChange = viewModel::onMinimumConfidenceChange,
        onDecimalSeparatorChange = viewModel::onDecimalSeparatorChange,
        modifier = modifier
    )
}

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.TIRAMISU)
private fun shouldRequestNotificationPermission(context: Context): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
        PackageManager.PERMISSION_GRANTED
