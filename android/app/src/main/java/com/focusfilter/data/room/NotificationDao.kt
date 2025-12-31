package com.focusfilter.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the notifications table.
 */
@Dao
interface NotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: NotificationEntity)

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAll(): Flow<List<NotificationEntity>>

    @Query("UPDATE notifications SET userClassification = :category WHERE id = :id")
    suspend fun updateUserClassification(id: Long, category: String)

    @Query("UPDATE notifications SET isActionable = :isActionable WHERE id = :id")
    suspend fun updateActionable(id: Long, isActionable: Boolean)

    @Query("DELETE FROM notifications")
    suspend fun clearAll()
}
