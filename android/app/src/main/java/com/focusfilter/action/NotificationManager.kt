package com.focusfilter.action

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.focusfilter.data.NotificationRepository
import com.focusfilter.models.ClassificationResult
import com.focusfilter.models.Notification
import com.focusfilter.ui.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FocusFilterNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: NotificationRepository
) {
    private val tag = "NotificationManager"
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val urgentChannel = NotificationChannel(
                URGENT_CHANNEL_ID,
                "Focus Filter - Urgent",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Urgent notifications that need immediate attention"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(urgentChannel)
        }
    }

    fun promoteNotification(
        notification: Notification,
        classification: ClassificationResult
    ) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, URGENT_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(notification.title)
            .setContentText(notification.body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        notificationManager.notify(notification.id.hashCode(), builder.build())
        Log.d(tag, "Promoted notification: ${notification.title}")
    }

    fun recordSuppressedNotification(
        notification: Notification,
        classification: ClassificationResult,
        isSystem: Boolean
    ) {
        Log.d(tag, "Suppressed notification: ${notification.title}")
        coroutineScope.launch {
            repository.insertNotification(notification, classification, isSystem)
        }
    }

    fun storeNotification(
        notification: Notification,
        classification: ClassificationResult,
        isSystem: Boolean
    ) {
        Log.d(tag, "Stored notification: ${notification.title}")
        coroutineScope.launch {
            repository.insertNotification(notification, classification, isSystem)
        }
    }

    companion object {
        const val URGENT_CHANNEL_ID = "focus_filter_urgent"
    }
}
