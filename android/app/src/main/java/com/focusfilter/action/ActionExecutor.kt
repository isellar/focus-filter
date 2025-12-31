package com.focusfilter.action

import android.util.Log
import com.focusfilter.models.ClassificationResult
import com.focusfilter.models.Notification
import dagger.hilt.android.scopes.ServiceScoped
import javax.inject.Inject

/**
 * Executes actions based on notification classification.
 */
@ServiceScoped
class ActionExecutor @Inject constructor(
    private val notificationManager: FocusFilterNotificationManager
) {
    private val tag = "ActionExecutor"

    /**
     * Promotes notification with high priority.
     */
    fun executeUrgent(notification: Notification, classification: ClassificationResult) {
        Log.d(tag, "Executing URGENT action for: ${notification.title}")
        notificationManager.promoteNotification(notification, classification)
    }

    /**
     * Stores notification for later review at an opportune time.
     */
    fun executeInformational(notification: Notification, classification: ClassificationResult) {
        Log.d(tag, "Executing INFORMATIONAL action for: ${notification.title}")
        notificationManager.storeNotification(notification, classification)
    }

    /**
     * Stores notification for background context, does not surface to user.
     */
    fun executeBackground(notification: Notification, classification: ClassificationResult) {
        Log.d(tag, "Executing BACKGROUND action for: ${notification.title}")
        notificationManager.storeNotification(notification, classification)
    }

    /**
     * Suppresses notification and records it.
     */
    fun executeIrrelevant(notification: Notification, classification: ClassificationResult) {
        Log.d(tag, "Executing IRRELEVANT action for: ${notification.title}")
        notificationManager.recordSuppressedNotification(notification, classification)
    }
}
