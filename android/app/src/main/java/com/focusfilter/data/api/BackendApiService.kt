package com.focusfilter.data.api

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Retrofit interface for backend API calls.
 */
interface BackendApiService {
    @POST("api/v1/notifications/classify")
    suspend fun classify(
        @Header("X-API-Key") apiKey: String,
        @Body request: ClassificationRequest
    ): ClassificationResponse
}

/**
 * Request model for classification.
 */
data class ClassificationRequest(
    val title: String,
    val body: String,
    val app_name: String,
    val package_name: String,
    val timestamp: Long,
    val extras: Map<String, String> = emptyMap()
)

/**
 * Response model for classification.
 */
data class ClassificationResponse(
    val notification_id: String,
    val category: String, // "URGENT", "IRRELEVANT", "LESS_URGENT"
    val confidence: Float,
    val reasoning: String
)
