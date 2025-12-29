package com.focusfilter.action

import android.util.Log
import com.focusfilter.models.ClassificationResult
import com.focusfilter.models.Notification
import com.focusfilter.models.NotificationCategory
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
     * Executes action for URGENT notifications.
     * Promotes notification with high priority.
     */
    suspend fun executeUrgent(
        notification: Notification,
        classification: ClassificationResult
    ) {
        Log.d(tag, "Executing URGENT action for: ${notification.title}")
        notificationManager.promoteNotification(notification, classification)
    }

    /**
     * Executes action for IRRELEVANT notifications.
     * Suppresses notification (already cancelled).
     */
    suspend fun executeIrrelevant(
        notification: Notification,
        classification: ClassificationResult
    ) {
        Log.d(tag, "Executing IRRELEVANT action for: ${notification.title}")
        // Notification already cancelled, just log
        notificationManager.recordSuppressedNotification(notification, classification)
    }

    /**
     * Executes action for LESS_URGENT notifications.
     * Stores notification for later review.
     */
    suspend fun executeLessUrgent(
        notification: Notification,
        classification: ClassificationResult
    ) {
        Log.d(tag, "Executing LESS_URGENT action for: ${notification.title}")
        notificationManager.storeNotification(notification, classification)
    }
}
