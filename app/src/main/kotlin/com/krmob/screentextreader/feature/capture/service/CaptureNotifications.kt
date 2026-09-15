package com.krmob.screentextreader.feature.capture.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.krmob.screentextreader.R
import com.krmob.screentextreader.core.permissions.checkNotificationPermission
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private const val CHANNEL_ID = "screen_capture"
private const val OPEN_APP_REQUEST_CODE = 1
private const val STOP_CAPTURE_REQUEST_CODE = 2

/** Builds the ongoing notification that a media projection foreground service must show. */
class CaptureNotifications @Inject constructor(@param:ApplicationContext private val context: Context) {
    private val notificationManager = NotificationManagerCompat.from(context)

    fun createChannel() {
        notificationManager.createNotificationChannel(
            NotificationChannelCompat
                .Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_LOW)
                .setName(context.getString(R.string.notification_channel_name))
                .setDescription(context.getString(R.string.notification_channel_description))
                .build()
        )
    }

    fun build(isPaused: Boolean, lineCount: Int): Notification {
        val title = if (isPaused) R.string.notification_title_paused else R.string.notification_title_reading
        return NotificationCompat
            .Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(title))
            .setContentText(
                context.resources.getQuantityString(R.plurals.notification_recognized_lines, lineCount, lineCount)
            ).setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setContentIntent(openAppIntent())
            .addAction(R.drawable.ic_stop, context.getString(R.string.notification_action_stop), stopCaptureIntent())
            .build()
    }

    /** Refreshes the notification; skipped when the user has not allowed notifications. */
    fun update(isPaused: Boolean, lineCount: Int) {
        if (!context.checkNotificationPermission()) return
        notificationManager.notify(NOTIFICATION_ID, build(isPaused = isPaused, lineCount = lineCount))
    }

    private fun openAppIntent(): PendingIntent? =
        context.packageManager.getLaunchIntentForPackage(context.packageName)?.let { launchIntent ->
            PendingIntent.getActivity(context, OPEN_APP_REQUEST_CODE, launchIntent, PendingIntent.FLAG_IMMUTABLE)
        }

    private fun stopCaptureIntent(): PendingIntent = PendingIntent.getService(
        context,
        STOP_CAPTURE_REQUEST_CODE,
        ScreenCaptureService.stopIntent(context),
        PendingIntent.FLAG_IMMUTABLE
    )

    companion object {
        const val NOTIFICATION_ID = 1
    }
}
