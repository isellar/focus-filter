package com.focusfilter.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

/**
 * Notification Assistant Service for re-ranking notifications and generating smart replies.
 * This is a step above NotificationListenerService and allows more control.
 */
// TODO: Revert this to extend NotificationAssistantService when build issues with Android SDK 35 are resolved.
// For an unknown reason, the compiler cannot resolve NotificationAssistantService even with
// compileSdk=35. As a temporary workaround to keep the project buildable, this service
// extends NotificationListenerService. The full assistant features cannot be implemented
// until this is fixed.
class FocusFilterNotificationAssistantService : NotificationListenerService() {

    private val tag = "NotificationAssistantService"

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        Log.d(tag, "ASSISTANT: Notification posted: ${sbn?.packageName}")
        // Re-ranking logic will be implemented here
    }

    /*
    override fun onNotificationSnoozedUntilContext(
        sbn: StatusBarNotification?,
        snoozeCriterion: SnoozeCriterion?
    ) {
        super.onNotificationSnoozedUntilContext(sbn, snoozeCriterion)
        Log.d(tag, "Notification snoozed: ${sbn?.packageName}")
    }

    override fun onNotificationsSeen(notifications: MutableList<StatusBarNotification>?) {
        super.onNotificationsSeen(notifications)
        Log.d(tag, "Notifications seen: ${notifications?.size}")
    }
    */
}
