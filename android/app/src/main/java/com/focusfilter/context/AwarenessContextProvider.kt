package com.focusfilter.context

import android.util.Log
import com.focusfilter.models.ActivityType
import com.focusfilter.models.LocationContext
import com.focusfilter.models.WeatherContext
import com.google.android.gms.awareness.Awareness
import com.google.android.gms.awareness.state.Weather
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.tasks.Tasks
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides context from Google Awareness API.
 * Includes activity detection, weather, and location.
 */
@Singleton
class AwarenessContextProvider @Inject constructor(
    @ApplicationContext private val context: android.content.Context
) {
    private val tag = "AwarenessContextProvider"

    /**
     * Gets the current user activity.
     */
    suspend fun getCurrentActivity(): ActivityType? = withContext(Dispatchers.IO) {
        try {
            val awarenessClient = Awareness.getSnapshotClient(context)
            val activityResult = Tasks.await(
                ActivityRecognition.getClient(context).requestActivityUpdates(
                    0L,
                    com.google.android.gms.location.ActivityTransitionRequest(
                        listOf()
                    )
                )
            )

            // This is simplified - actual implementation would use ActivityRecognition API
            // For now, return null (will be implemented with proper API calls)
            null
        } catch (e: Exception) {
            Log.e(tag, "Error getting activity", e)
            null
        }
    }

    /**
     * Gets the current weather context.
     */
    suspend fun getWeatherContext(): WeatherContext? = withContext(Dispatchers.IO) {
        try {
            val awarenessClient = Awareness.getSnapshotClient(context)
            val weatherResult = Tasks.await(awarenessClient.weather)

            val weather = weatherResult.snapshot?.weather
            if (weather != null) {
                WeatherContext(
                    condition = getWeatherCondition(weather),
                    temperature = weather.temperature?.celsius
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(tag, "Error getting weather", e)
            null
        }
    }

    /**
     * Gets the current location context.
     */
    suspend fun getLocationContext(): LocationContext? = withContext(Dispatchers.IO) {
        try {
            // Simplified - actual implementation would use location services
            // For now, return null (will be implemented with proper API calls)
            null
        } catch (e: Exception) {
            Log.e(tag, "Error getting location", e)
            null
        }
    }

    private fun getWeatherCondition(weather: Weather): String {
        return when {
            weather.conditions.contains(Weather.CONDITION_RAINY) -> "raining"
            weather.conditions.contains(Weather.CONDITION_SNOWY) -> "snowing"
            weather.conditions.contains(Weather.CONDITION_SUNNY) -> "sunny"
            weather.conditions.contains(Weather.CONDITION_CLOUDY) -> "cloudy"
            else -> "unknown"
        }
    }
}
