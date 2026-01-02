package com.focusfilter.processor

import android.util.Log
import com.focusfilter.action.ActionExecutor
import com.focusfilter.context.ContextProvider
import com.focusfilter.models.ClassificationResult
import com.focusfilter.models.Notification
import com.focusfilter.models.NotificationCategory
import com.focusfilter.reasoning.ReasoningEngine
import dagger.hilt.android.scopes.ServiceScoped
import javax.inject.Inject

@ServiceScoped
class NotificationProcessor @Inject constructor(
    private val contextProvider: ContextProvider,
    private val reasoningEngine: ReasoningEngine,
    private val actionExecutor: ActionExecutor
) {
    private val tag = "NotificationProcessor"

    suspend fun processNotification(notification: Notification, isSystemNotification: Boolean) {
        try {
            Log.d(tag, "Processing notification: ${notification.title}")

            // If it's a system notification, automatically classify as Urgent
            val classification = if (isSystemNotification) {
                Log.d(tag, "System notification detected, classifying as URGENT.")
                ClassificationResult(
                    notificationId = notification.id,
                    category = NotificationCategory.URGENT,
                    confidence = 1.0f,
                    reasoning = "System-level notification."
                )
            } else {
                val context = contextProvider.getCurrentContext()
                reasoningEngine.classify(notification, context)
            }

            Log.d(tag, "Classification: ${classification.category} (confidence: ${classification.confidence})")

            // Execute action based on classification
            when (classification.category) {
                NotificationCategory.URGENT -> actionExecutor.executeUrgent(notification, classification, isSystemNotification)
                NotificationCategory.INFORMATIONAL -> actionExecutor.executeInformational(notification, classification, isSystemNotification)
                NotificationCategory.BACKGROUND -> actionExecutor.executeBackground(notification, classification, isSystemNotification)
                NotificationCategory.IRRELEVANT -> actionExecutor.executeIrrelevant(notification, classification, isSystemNotification)
            }

            Log.d(tag, "Notification processed successfully")
        } catch (e: Exception) {
            Log.e(tag, "Error processing notification", e)
            // Fallback: treat as background
            actionExecutor.executeBackground(
                notification,
                ClassificationResult(
                    notificationId = notification.id,
                    category = NotificationCategory.BACKGROUND,
                    confidence = 0.5f,
                    reasoning = "Error: ${e.message}"
                ),
                isSystemNotification // Pass the flag in case of error
            )
        }
    }
}
