package com.focusfilter.data

import com.focusfilter.data.room.NotificationDao
import com.focusfilter.data.room.NotificationEntity
import com.focusfilter.models.ClassificationResult
import com.focusfilter.models.Notification
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing notification storage.
 * It abstracts the data source (Room, and later AppSearch) from the rest of the app.
 */
@Singleton
class NotificationRepository @Inject constructor(
    private val notificationDao: NotificationDao
) {

    /**
     * Inserts a processed notification into the local database.
     */
    suspend fun insertNotification(
        notification: Notification,
        classification: ClassificationResult
    ) {
        val entity = NotificationEntity(
            title = notification.title,
            body = notification.body,
            appName = notification.appName,
            packageName = notification.packageName,
            timestamp = notification.timestamp,
            classification = classification.category.name,
            reasoning = classification.reasoning
        )
        notificationDao.insert(entity)
    }

    /**
     * Retrieves all notifications from the database.
     */
    fun getAllNotifications(): Flow<List<NotificationEntity>> {
        return notificationDao.getAll()
    }

    /**
     * Clears all notifications from the database.
     */
    suspend fun clearAllNotifications() {
        notificationDao.clearAll()
    }
}
