package com.focusfilter.action

import android.util.Log
import com.focusfilter.models.ClassificationResult
import com.focusfilter.models.Notification
import dagger.hilt.android.scopes.ServiceScoped
import javax.inject.Inject

@ServiceScoped
class ActionExecutor @Inject constructor(
    private val notificationManager: FocusFilterNotificationManager
) {
    private val tag = "ActionExecutor"

    fun executeUrgent(notification: Notification, classification: ClassificationResult, isSystem: Boolean) {
        Log.d(tag, "Executing URGENT action for: ${notification.title}")
        notificationManager.promoteNotification(notification, classification)
    }

    fun executeInformational(notification: Notification, classification: ClassificationResult, isSystem: Boolean) {
        Log.d(tag, "Executing INFORMATIONAL action for: ${notification.title}")
        notificationManager.storeNotification(notification, classification, isSystem)
    }

    fun executeBackground(notification: Notification, classification: ClassificationResult, isSystem: Boolean) {
        Log.d(tag, "Executing BACKGROUND action for: ${notification.title}")
        notificationManager.storeNotification(notification, classification, isSystem)
    }

    fun executeIrrelevant(notification: Notification, classification: ClassificationResult, isSystem: Boolean) {
        Log.d(tag, "Executing IRRELEVANT action for: ${notification.title}")
        notificationManager.recordSuppressedNotification(notification, classification, isSystem)
    }
}
