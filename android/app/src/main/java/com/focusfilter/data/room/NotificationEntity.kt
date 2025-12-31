package com.focusfilter.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.focusfilter.models.NotificationCategory

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val body: String,
    val appName: String,
    val packageName: String,
    val timestamp: Long,
    val aiClassification: String,
    val reasoning: String? = null,
    var userClassification: String? = null,
    var isActionable: Boolean = false
)
