package com.focusfilter.reasoning

import android.content.Context
import android.util.Log
import com.focusfilter.models.ClassificationResult
import com.focusfilter.models.Notification
import com.focusfilter.models.NotificationCategory
import com.focusfilter.models.UserContext
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnDeviceReasoningEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val backendFallback: BackendReasoningEngine
) : ReasoningEngine {

    private val tag = "OnDeviceReasoningEngine"
    private var isAICoreAvailable: Boolean = false

    init {
        // Placeholder check. This will be updated with the real ML Kit implementation.
        isAICoreAvailable = false
        Log.w(tag, "AICore availability check is not yet implemented. Defaulting to unavailable.")
    }

    override suspend fun classify(
        notification: Notification,
        context: UserContext
    ): ClassificationResult {
        if (isAICoreAvailable) {
            // This block will be implemented in a future step
            Log.d(tag, "AICore is available, but on-device classification is not yet implemented.")
            return ClassificationResult(
                notification.id,
                NotificationCategory.BACKGROUND,
                0.5f,
                "On-device classification not implemented."
            )
        } else {
            Log.d(tag, "Using backend fallback for classification.")
            return backendFallback.classify(notification, context)
        }
    }
}
