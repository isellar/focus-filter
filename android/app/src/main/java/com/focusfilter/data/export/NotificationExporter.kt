package com.focusfilter.data.export

import com.focusfilter.data.NotificationRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles exporting notification data to a JSON format.
 */
@Singleton
class NotificationExporter @Inject constructor(
    private val repository: NotificationRepository
) {

    /**
     * Fetches all notifications from the repository and converts them to a JSON string.
     *
     * @return A JSON formatted string of all notifications, or null if an error occurs.
     */
    suspend fun exportToJson(): String? {
        return try {
            val notifications = repository.getAllNotifications().first()
            val gson = Gson()
            gson.toJson(notifications)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
