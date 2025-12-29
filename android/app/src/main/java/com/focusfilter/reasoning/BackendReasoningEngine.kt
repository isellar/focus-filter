package com.focusfilter.reasoning

import com.focusfilter.models.ClassificationResult
import com.focusfilter.models.Notification
import com.focusfilter.models.NotificationCategory
import com.focusfilter.models.UserContext
import com.focusfilter.data.api.BackendApiService
import com.focusfilter.data.api.ClassificationRequest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Backend API reasoning engine (fallback when AICore unavailable).
 */
@Singleton
class BackendReasoningEngine @Inject constructor(
    private val apiService: BackendApiService
) : ReasoningEngine {

    override suspend fun classify(
        notification: Notification,
        context: UserContext
    ): ClassificationResult {
        return try {
            val request = ClassificationRequest(
                title = notification.title,
                body = notification.body,
                app_name = notification.appName,
                package_name = notification.packageName,
                timestamp = notification.timestamp,
                extras = notification.extras
            )

            // Pass a placeholder API key
            val response = apiService.classify("", request)

            ClassificationResult(
                notificationId = response.notification_id,
                category = when (response.category) {
                    "URGENT" -> NotificationCategory.URGENT
                    "IRRELEVANT" -> NotificationCategory.IRRELEVANT
                    else -> NotificationCategory.LESS_URGENT
                },
                confidence = response.confidence,
                reasoning = response.reasoning
            )
        } catch (e: Exception) {
            // Fallback to simple classification
            ClassificationResult(
                notificationId = notification.id,
                category = NotificationCategory.LESS_URGENT,
                confidence = 0.5f,
                reasoning = "Backend API error: ${e.message}"
            )
        }
    }
}
