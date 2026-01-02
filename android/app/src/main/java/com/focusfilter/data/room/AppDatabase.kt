package com.focusfilter.data.room

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * The Room database for the application.
 * This database serves as a fallback storage for notifications.
 */
@Database(
    entities = [NotificationEntity::class],
    version = 3, // Incremented version from 2 to 3
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao
}
