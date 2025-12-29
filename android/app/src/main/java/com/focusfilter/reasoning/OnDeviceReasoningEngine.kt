package com.focusfilter.reasoning

import android.content.Context
import android.util.Log
import com.focusfilter.models.ClassificationResult
import com.focusfilter.models.Notification
import com.focusfilter.models.NotificationCategory
import com.focusfilter.models.UserContext
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.generativeModel
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

    /**
     * Checks if AICore (Gemini Nano) is available on this device.
     */
    private fun checkAICoreAvailability() {
        try {
            // Check if AICore is available
            // Note: This is a placeholder - actual AICore availability check
            // may require different API calls depending on the SDK version
            isAICoreAvailable = try {
                // Try to initialize GenerativeModel
                generativeModel = generativeModel {
                    model = "gemini-nano"
                }
                true
            } catch (e: Exception) {
                Log.w(tag, "AICore not available: ${e.message}")
                false
            }
        } catch (e: Exception) {
            Log.e(tag, "Error checking AICore availability", e)
            isAICoreAvailable = false
        }
    }

    override suspend fun classify(
        notification: Notification,
        context: UserContext
    ): ClassificationResult {
        return if (isAICoreAvailable && generativeModel != null) {
            classifyWithAICore(notification, context)
        } else {
            // Fallback to backend API
            Log.d(tag, "AICore unavailable, using backend fallback")
            backendFallback?.classify(notification, context)
                ?: throw IllegalStateException("No reasoning engine available")
        }
    }

    /**
     * Classifies notification using Gemini Nano on-device.
     */
    private suspend fun classifyWithAICore(
        notification: Notification,
        context: UserContext
    ): ClassificationResult {
        val prompt = buildPrompt(notification, context)

        return try {
            val model = generativeModel ?: throw IllegalStateException("Model not initialized")
            val response = model.generateContent(prompt)
            val text = response.text ?: throw IllegalStateException("Empty response from model")

            parseClassificationResponse(notification.id, text)
        } catch (e: Exception) {
            Log.e(tag, "Error classifying with AICore", e)
            // Fallback to backend
            backendFallback?.classify(notification, context)
                ?: ClassificationResult(
                    notificationId = notification.id,
                    category = NotificationCategory.LESS_URGENT,
                    confidence = 0.5f,
                    reasoning = "Error: ${e.message}"
                )
        }
    }

    /**
     * Builds the prompt for the reasoning engine.
     */
    private fun buildPrompt(
        notification: Notification,
        context: UserContext
    ): String {
        return """
            Given context:
            ${context.toPromptString()}

            Should I alert the user about this notification:
            - Title: ${notification.title}
            - Body: ${notification.body}
            - App: ${notification.appName}

            Classify as: URGENT, IRRELEVANT, or LESS_URGENT.
            Provide confidence (0.0-1.0) and reasoning.

            Format your response as:
            CATEGORY: [URGENT|IRRELEVANT|LESS_URGENT]
            CONFIDENCE: [0.0-1.0]
            REASONING: [explanation]
        """.trimIndent()
    }

    /**
     * Parses the classification response from the model.
     */
    private fun parseClassificationResponse(
        notificationId: String,
        response: String
    ): ClassificationResult {
        // Parse the response
        // This is a simplified parser - you may want to use JSON or more structured parsing
        val category = when {
            response.contains("URGENT", ignoreCase = true) -> NotificationCategory.URGENT
            response.contains("IRRELEVANT", ignoreCase = true) -> NotificationCategory.IRRELEVANT
            else -> NotificationCategory.LESS_URGENT
        }

        val confidenceMatch = Regex("CONFIDENCE:\\s*([0-9.]+)").find(response)
        val confidence = confidenceMatch?.groupValues?.get(1)?.toFloatOrNull() ?: 0.7f

        val reasoningMatch = Regex("REASONING:\\s*(.+)", RegexOption.DOT_MATCHES_ALL).find(response)
        val reasoning = reasoningMatch?.groupValues?.get(1)?.trim() 
            ?: "Classified based on content analysis"

        return ClassificationResult(
            notificationId = notificationId,
            category = category,
            confidence = confidence.coerceIn(0f, 1f),
            reasoning = reasoning
        )
    }
}
