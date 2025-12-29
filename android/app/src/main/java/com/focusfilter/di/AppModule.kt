package com.focusfilter.di

import com.focusfilter.data.api.BackendApiService
import com.focusfilter.reasoning.BackendReasoningEngine
import com.focusfilter.reasoning.OnDeviceReasoningEngine
import com.focusfilter.reasoning.ReasoningEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        // Base URL will be configured from settings
        return Retrofit.Builder()
            .baseUrl("http://localhost:8000/") // Default, should be configurable
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideBackendApiService(retrofit: Retrofit): BackendApiService {
        return retrofit.create(BackendApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideBackendReasoningEngine(
        apiService: BackendApiService
    ): BackendReasoningEngine {
        return BackendReasoningEngine(apiService)
    }

    @Provides
    @Singleton
    fun provideReasoningEngine(
        onDeviceEngine: OnDeviceReasoningEngine
    ): ReasoningEngine {
        // Primary engine is on-device, which has backend fallback built-in
        return onDeviceEngine
    }
}
