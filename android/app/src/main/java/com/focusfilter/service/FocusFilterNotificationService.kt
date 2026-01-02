package com.focusfilter.service

import android.app.Notification as AndroidNotification
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

@AndroidEntryPoint
class FocusFilterNotificationService : NotificationListenerService() {

    private val tag = "FocusFilterNotificationService"
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    @Inject
    lateinit var notificationProcessor: NotificationProcessor
    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val isSystemNotification = (sbn.notification.flags and AndroidNotification.FLAG_ONGOING_EVENT != 0) ||
                                   sbn.packageName == "android" ||
                                   sbn.packageName.startsWith("com.android.systemui")

        val notification = extractNotification(sbn)
        Log.d(tag, "Notification intercepted: ${notification.title} from ${notification.appName}")

        serviceScope.launch {
            val isPassthroughEnabled = settingsRepository.passthroughEnabled.first()

            // Never cancel system notifications. Only cancel others if passthrough is disabled.
            if (!isPassthroughEnabled && !isSystemNotification) {
                cancelNotification(sbn.key)
            } else {
                Log.d(tag, "Not cancelling notification. Passthrough: $isPassthroughEnabled, System: $isSystemNotification")
            }

            try {
                notificationProcessor.processNotification(notification, isSystemNotification)
            } catch (e: Exception) {
                Log.e(tag, "Error processing notification", e)
            }
        }
    }

    private fun extractNotification(sbn: StatusBarNotification): Notification {
        val extras = sbn.notification.extras
        val title = extras.getCharSequence(AndroidNotification.EXTRA_TITLE)?.toString() ?: ""
        val body = extras.getCharSequence(AndroidNotification.EXTRA_TEXT)?.toString() ?: ""
        val appName = sbn.packageName // Placeholder

        val notificationExtras = mutableMapOf<String, String>()
        extras.keySet()?.forEach { key ->
            extras.get(key)?.toString()?.let { value -> notificationExtras[key] = value }
        }

        return Notification(
            id = sbn.key,
            title = title,
            body = body,
            appName = appName,
            packageName = sbn.packageName,
            timestamp = sbn.postTime,
            extras = notificationExtras
        )
    }
}
