package com.focusfilter.models

import java.util.UUID

/**
 * Represents a notification intercepted by the app.
 */
data class Notification(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val body: String,
    val appName: String,
    val packageName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val extras: Map<String, String> = emptyMap()
)
