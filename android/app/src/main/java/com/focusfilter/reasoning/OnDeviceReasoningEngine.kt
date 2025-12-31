package com.focusfilter.reasoning

import android.content.Context
import android.util.Log
import com.focusfilter.models.ClassificationResult
import com.focusfilter.models.Notification
import com.focusfilter.models.NotificationCategory
import com.focusfilter.models.UserContext
import com.google.ai.client.generativeai.GenerativeModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * On-device reasoning engine using Gemini Nano via AICore.
 * Falls back to backend API if AICore is unavailable.
 */
@Singleton
class OnDeviceReasoningEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val backendFallback: BackendReasoningEngine? = null
) : ReasoningEngine {

    private val tag = "OnDeviceReasoningEngine"
    private var generativeModel: GenerativeModel? = null
    private var isAICoreAvailable: Boolean = false

    init {
        checkAICoreAvailability()
    }

    private fun checkAICoreAvailability() {
        isAICoreAvailable = false // Placeholder
        Log.w(tag, "AICore availability check is not implemented. Defaulting to unavailable.")
    }

    override suspend fun classify(
        notification: Notification,
        context: UserContext
    ): ClassificationResult {
        return if (isAICoreAvailable && generativeModel != null) {
            classifyWithAICore(notification, context)
        } else {
            Log.d(tag, "AICore unavailable, using backend fallback")
            backendFallback?.classify(notification, context)
                ?: throw IllegalStateException("No reasoning engine available")
        }
    }

    private suspend fun classifyWithAICore(
        notification: Notification,
        context: UserContext
    ): ClassificationResult {
        val prompt = buildPrompt(notification, context)
        return try {
            // val model = generativeModel ?: throw IllegalStateException("Model not initialized")
            // val response = model.generateContent(prompt)
            // val text = response.text ?: throw IllegalStateException("Empty response from model")
            // parseClassificationResponse(notification.id, text)
            throw NotImplementedError("AICore classification is not yet implemented.")
        } catch (e: Exception) {
            Log.e(tag, "Error classifying with AICore", e)
            backendFallback?.classify(notification, context)
                ?: ClassificationResult(
                    notificationId = notification.id,
                    category = NotificationCategory.BACKGROUND,
                    confidence = 0.5f,
                    reasoning = "Error: ${e.message}"
                )
        }
    }

    private fun buildPrompt(
        notification: Notification,
        context: UserContext
    ): String {
        return """
            Given context:
            ${context.toPromptString()}

            Classify this notification:
            - Title: ${notification.title}
            - Body: ${notification.body}
            - App: ${notification.appName}

            Classify as: URGENT, INFORMATIONAL, BACKGROUND, or IRRELEVANT.
            Provide confidence (0.0-1.0) and reasoning.

            Format your response as:
            CATEGORY: [CATEGORY]
            CONFIDENCE: [0.0-1.0]
            REASONING: [explanation]
        """.trimIndent()
    }

    private fun parseClassificationResponse(
        notificationId: String,
        response: String
    ): ClassificationResult {
        val category = when {
            response.contains("URGENT", true) -> NotificationCategory.URGENT
            response.contains("INFORMATIONAL", true) -> NotificationCategory.INFORMATIONAL
            response.contains("BACKGROUND", true) -> NotificationCategory.BACKGROUND
            else -> NotificationCategory.IRRELEVANT
        }

        val confidenceMatch = Regex("CONFIDENCE:\\s*([0-9.]+)").find(response)
        val confidence = confidenceMatch?.groupValues?.get(1)?.toFloatOrNull() ?: 0.7f

        val reasoningMatch = Regex("REASONING:\\s*(.+)", RegexOption.DOT_MATCHES_ALL).find(response)
        val reasoning = reasoningMatch?.groupValues?.get(1)?.trim() ?: "No reasoning provided."

        return ClassificationResult(
            notificationId = notificationId,
            category = category,
            confidence = confidence.coerceIn(0f, 1f),
            reasoning = reasoning
        )
    }
}
