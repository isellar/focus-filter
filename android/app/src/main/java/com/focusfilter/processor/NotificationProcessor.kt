package com.focusfilter.processor

import android.util.Log
import com.focusfilter.models.Notification
import com.focusfilter.models.NotificationCategory
import com.focusfilter.context.ContextProvider
import com.focusfilter.reasoning.ReasoningEngine
import com.focusfilter.action.ActionExecutor
import dagger.hilt.android.scopes.ServiceScoped
import javax.inject.Inject

/**
 * Processes notifications through the agentic loop:
 * 1. Gather context
 * 2. Classify notification
 * 3. Execute action based on classification
 */
@ServiceScoped
class NotificationProcessor @Inject constructor(
    private val contextProvider: ContextProvider,
    private val reasoningEngine: ReasoningEngine,
    private val actionExecutor: ActionExecutor
) {
    private val tag = "NotificationProcessor"

    /**
     * Processes a notification through the full agentic pipeline.
     */
    suspend fun processNotification(notification: Notification) {
        try {
            Log.d(tag, "Processing notification: ${notification.title}")

            // Step 1: Gather context
            val context = contextProvider.getCurrentContext()
            Log.d(tag, "Context: ${context.toPromptString()}")

            // Step 2: Classify notification
            val classification = reasoningEngine.classify(notification, context)
            Log.d(tag, "Classification: ${classification.category} (confidence: ${classification.confidence})")

            // Step 3: Execute action based on classification
            when (classification.category) {
                NotificationCategory.URGENT -> {
                    actionExecutor.executeUrgent(notification, classification)
                }
                NotificationCategory.IRRELEVANT -> {
                    actionExecutor.executeIrrelevant(notification, classification)
                }
                NotificationCategory.LESS_URGENT -> {
                    actionExecutor.executeLessUrgent(notification, classification)
                }
            }

            Log.d(tag, "Notification processed successfully")
        } catch (e: Exception) {
            Log.e(tag, "Error processing notification", e)
            // Fallback: treat as less urgent
            actionExecutor.executeLessUrgent(
                notification,
                com.focusfilter.models.ClassificationResult(
                    notificationId = notification.id,
                    category = NotificationCategory.LESS_URGENT,
                    confidence = 0.5f,
                    reasoning = "Error: ${e.message}"
                )
            )
        }
    }
}
