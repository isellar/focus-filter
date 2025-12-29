package com.focusfilter.context

import android.content.ContentResolver
import android.provider.CalendarContract
import android.util.Log
import com.focusfilter.models.CalendarContext
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides calendar context.
 * Checks for current and upcoming meetings.
 */
@Singleton
class CalendarContextProvider @Inject constructor(
    @ApplicationContext private val context: android.content.Context
) {
    private val tag = "CalendarContextProvider"
    private val contentResolver: ContentResolver = context.contentResolver

    /**
     * Gets the current calendar context.
     */
    suspend fun getCalendarContext(): CalendarContext? = withContext(Dispatchers.IO) {
        try {
            val now = System.currentTimeMillis()
            val oneHourFromNow = now + (60 * 60 * 1000)

            val projection = arrayOf(
                CalendarContract.Events.TITLE,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.DTEND
            )

            val selection = "${CalendarContract.Events.DTSTART} <= ? AND ${CalendarContract.Events.DTEND} >= ?"
            val selectionArgs = arrayOf(oneHourFromNow.toString(), now.toString())

            val cursor = contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                "${CalendarContract.Events.DTSTART} ASC"
            )

            cursor?.use {
                if (it.moveToFirst()) {
                    val startTime = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events.DTSTART))
                    val endTime = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events.DTEND))
                    
                    val isCurrentMeeting = now >= startTime && now <= endTime
                    val minutesUntilMeeting = if (!isCurrentMeeting) {
                        ((startTime - now) / (60 * 1000)).toInt()
                    } else {
                        null
                    }

                    CalendarContext(
                        hasUpcomingMeeting = true,
                        nextMeetingMinutes = minutesUntilMeeting,
                        currentMeeting = isCurrentMeeting
                    )
                } else {
                    CalendarContext(
                        hasUpcomingMeeting = false,
                        nextMeetingMinutes = null,
                        currentMeeting = false
                    )
                }
            } ?: CalendarContext(
                hasUpcomingMeeting = false,
                nextMeetingMinutes = null,
                currentMeeting = false
            )
        } catch (e: Exception) {
            Log.e(tag, "Error getting calendar context", e)
            null
        }
    }
}
