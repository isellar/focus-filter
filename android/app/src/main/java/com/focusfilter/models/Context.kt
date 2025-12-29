package com.focusfilter.models

/**
 * User context for agentic decision-making.
 */
data class UserContext(
    val activity: ActivityType? = null,
    val weather: WeatherContext? = null,
    val location: LocationContext? = null,
    val health: HealthContext? = null,
    val calendar: CalendarContext? = null
) {
    /**
     * Builds a context string for the reasoning engine prompt.
     */
    fun toPromptString(): String {
        val parts = mutableListOf<String>()
        
        activity?.let { parts.add("Activity: ${it.name}") }
        weather?.let { parts.add("Weather: ${it.description}") }
        location?.let { parts.add("Location: ${it.name}") }
        health?.let { parts.add("Health: ${it.description}") }
        calendar?.let { parts.add("Calendar: ${it.description}") }
        
        return if (parts.isEmpty()) {
            "No context available"
        } else {
            parts.joinToString(", ")
        }
    }
}

/**
 * User activity type.
 */
enum class ActivityType {
    WALKING,
    DRIVING,
    STILL,
    RUNNING,
    CYCLING,
    UNKNOWN
}

/**
 * Weather context.
 */
data class WeatherContext(
    val condition: String, // "raining", "sunny", etc.
    val temperature: Float? = null
) {
    val description: String
        get() = if (temperature != null) {
            "$condition, ${temperature}°C"
        } else {
            condition
        }
}

/**
 * Location context.
 */
data class LocationContext(
    val name: String, // "Home", "Work", etc.
    val latitude: Double? = null,
    val longitude: Double? = null
)

/**
 * Health context.
 */
data class HealthContext(
    val workoutStatus: WorkoutStatus? = null,
    val sleepWindow: SleepWindow? = null
) {
    val description: String
        get() = buildString {
            workoutStatus?.let { append("Just finished workout, ") }
            sleepWindow?.let { append("In sleep window, ") }
            if (isEmpty()) append("Normal")
        }
}

enum class WorkoutStatus {
    JUST_FINISHED,
    IN_PROGRESS,
    NONE
}

data class SleepWindow(
    val isActive: Boolean,
    val startTime: Long? = null,
    val endTime: Long? = null
)

/**
 * Calendar context.
 */
data class CalendarContext(
    val hasUpcomingMeeting: Boolean,
    val nextMeetingMinutes: Int? = null,
    val currentMeeting: Boolean = false
) {
    val description: String
        get() = when {
            currentMeeting -> "In meeting"
            hasUpcomingMeeting && nextMeetingMinutes != null -> "Meeting in $nextMeetingMinutes minutes"
            hasUpcomingMeeting -> "Meeting soon"
            else -> "No meetings"
        }
}
