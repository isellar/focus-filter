package com.focusfilter.action

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.focusfilter.R
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

/**
 * Manages notification display and storage.
 */
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

    /**
     * Creates notification channels for the app.
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Urgent notifications channel
            val urgentChannel = NotificationChannel(
                URGENT_CHANNEL_ID,
                "Focus Filter - Urgent",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Urgent notifications that need immediate attention"
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(urgentChannel)

            // Agent summary channel
            val summaryChannel = NotificationChannel(
                SUMMARY_CHANNEL_ID,
                "Focus Filter - Summary",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Agent summary of actions taken"
            }
            notificationManager.createNotificationChannel(summaryChannel)
        }
    }

    /**
     * Promotes a notification as urgent.
     */
    fun promoteNotification(
        notification: Notification,
        classification: ClassificationResult
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(context, URGENT_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(notification.title)
            .setContentText(notification.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notification.body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(notification.id.hashCode(), notificationBuilder.build())
        Log.d(tag, "Promoted notification: ${notification.title} with reasoning: ${classification.reasoning}")
    }

    /**
     * Records a suppressed notification.
     */
    fun recordSuppressedNotification(
        notification: Notification,
        classification: ClassificationResult
    ) {
        Log.d(tag, "Suppressed notification: ${notification.title} with reasoning: ${classification.reasoning}")
        coroutineScope.launch {
            repository.insertNotification(notification, classification)
        }
    }

    /**
     * Stores a less urgent notification.
     */
    fun storeNotification(
        notification: Notification,
        classification: ClassificationResult
    ) {
        Log.d(tag, "Stored notification: ${notification.title} with reasoning: ${classification.reasoning}")
        coroutineScope.launch {
            repository.insertNotification(notification, classification)
        }
    }

    companion object {
        const val URGENT_CHANNEL_ID = "focus_filter_urgent"
        const val SUMMARY_CHANNEL_ID = "focus_filter_summary"
    }
}
