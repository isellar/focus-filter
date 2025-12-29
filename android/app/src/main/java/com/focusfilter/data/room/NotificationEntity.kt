package com.focusfilter.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a notification stored in the Room database.
 * This serves as a simpler, fallback storage solution compared to AppSearch.
 */
@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val body: String,
    val appName: String,
    val packageName: String,
    val timestamp: Long,
    val classification: String,
    val reasoning: String? = null
)
