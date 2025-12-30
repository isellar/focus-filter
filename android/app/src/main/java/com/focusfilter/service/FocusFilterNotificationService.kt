package com.focusfilter.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.focusfilter.data.SettingsRepository
import com.focusfilter.models.Notification
import com.focusfilter.processor.NotificationProcessor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Service that intercepts all notifications.
 * Extends NotificationListenerService to receive notification events.
 */
@AndroidEntryPoint
class FocusFilterNotificationService : NotificationListenerService() {

    private val tag = "FocusFilterNotificationService"
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    @Inject
    lateinit var notificationProcessor: NotificationProcessor
    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate() {
        super.onCreate()
        Log.d(tag, "Notification service created")
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(tag, "Notification listener connected")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(tag, "Notification listener disconnected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        
        if (sbn == null) {
            Log.w(tag, "Received null notification")
            return
        }

        // Extract notification data
        val notification = extractNotification(sbn)
        
        Log.d(tag, "Notification intercepted: ${notification.title} from ${notification.appName}")

        // Process notification asynchronously
        serviceScope.launch {
            // Check if passthrough is enabled. If so, do not cancel the original notification.
            val isPassthroughEnabled = settingsRepository.passthroughEnabled.first()
            if (!isPassthroughEnabled) {
                cancelNotification(sbn.key)
            } else {
                Log.d(tag, "Passthrough enabled, not cancelling original notification.")
            }

            try {
                notificationProcessor.processNotification(notification)
            } catch (e: Exception) {
                Log.e(tag, "Error processing notification", e)
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        // Handle notification removal if needed
    }

    /**
     * Extracts notification data from StatusBarNotification.
     */
    private fun extractNotification(sbn: StatusBarNotification): Notification {
        val androidNotification = sbn.notification
        val extras = androidNotification.extras

        val title = extras?.getCharSequence(android.app.Notification.EXTRA_TITLE)?.toString() ?: ""
        val body = extras?.getCharSequence(android.app.Notification.EXTRA_TEXT)?.toString() ?: ""
        val appName = sbn.packageName // Will be replaced with actual app name lookup
        val packageName = sbn.packageName
        val timestamp = sbn.postTime

        // Extract additional metadata
        val notificationExtras = mutableMapOf<String, String>()
        extras?.keySet()?.forEach { key ->
            extras.get(key)?.toString()?.let { value ->
                notificationExtras[key] = value
            }
        }

        return Notification(
            title = title,
            body = body,
            appName = appName,
            packageName = packageName,
            timestamp = timestamp,
            extras = notificationExtras
        )
    }
}
