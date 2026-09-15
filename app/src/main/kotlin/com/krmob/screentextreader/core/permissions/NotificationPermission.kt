package com.krmob.screentextreader.core.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * True when the app may post notifications; the runtime permission exists only since Android 13.
 * Named `check...Permission` so that Lint accepts it as the guard for calls that require the permission.
 */
fun Context.checkNotificationPermission(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
    ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
    PackageManager.PERMISSION_GRANTED
