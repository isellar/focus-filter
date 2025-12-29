package com.focusfilter.models

/**
 * Result of notification classification.
 */
data class ClassificationResult(
    val notificationId: String,
    val category: NotificationCategory,
    val confidence: Float, // 0.0 to 1.0
    val reasoning: String
)

/**
 * Notification classification categories.
 */
enum class NotificationCategory {
    URGENT,      // Important, needs immediate attention
    IRRELEVANT,  // Spam, ads, unimportant
    LESS_URGENT  // Can wait, store for later
}
