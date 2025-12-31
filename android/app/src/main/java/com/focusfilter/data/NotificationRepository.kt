package com.focusfilter.data

import com.focusfilter.data.room.NotificationDao
import com.focusfilter.data.room.NotificationEntity
import com.focusfilter.models.ClassificationResult
import com.focusfilter.models.Notification
import com.focusfilter.models.NotificationCategory
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing notification storage.
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
            aiClassification = classification.category.name,
            reasoning = classification.reasoning
        )
        notificationDao.insert(entity)
    }

    fun getAllNotifications(): Flow<List<NotificationEntity>> {
        return notificationDao.getAll()
    }

    suspend fun updateUserClassification(notificationId: Long, category: NotificationCategory) {
        notificationDao.updateUserClassification(notificationId, category.name)
    }

    suspend fun updateActionable(notificationId: Long, isActionable: Boolean) {
        notificationDao.updateActionable(notificationId, isActionable)
    }
}
