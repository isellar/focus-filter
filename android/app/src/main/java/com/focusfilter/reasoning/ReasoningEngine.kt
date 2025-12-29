package com.focusfilter.reasoning

import com.focusfilter.models.ClassificationResult
import com.focusfilter.models.Notification
import com.focusfilter.models.UserContext

/**
 * Interface for reasoning engines that classify notifications.
 */
interface ReasoningEngine {
    /**
     * Classifies a notification given user context.
     *
     * @param notification The notification to classify
     * @param context User context (activity, weather, calendar, etc.)
     * @return Classification result with category, confidence, and reasoning
     */
    suspend fun classify(
        notification: Notification,
        context: UserContext
    ): ClassificationResult
}
