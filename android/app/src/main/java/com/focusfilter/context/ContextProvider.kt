package com.focusfilter.context

import com.focusfilter.models.UserContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides user context for agentic decision-making.
 * Aggregates data from multiple sources (Awareness API, Health Connect, Calendar).
 */
@Singleton
class ContextProvider @Inject constructor(
    private val awarenessContextProvider: AwarenessContextProvider,
    private val healthContextProvider: HealthContextProvider,
    private val calendarContextProvider: CalendarContextProvider
) {
    /**
     * Gets the current user context.
     */
    suspend fun getCurrentContext(): UserContext {
        return UserContext(
            activity = awarenessContextProvider.getCurrentActivity(),
            weather = awarenessContextProvider.getWeatherContext(),
            location = awarenessContextProvider.getLocationContext(),
            health = healthContextProvider.getHealthContext(),
            calendar = calendarContextProvider.getCalendarContext()
        )
    }
}
