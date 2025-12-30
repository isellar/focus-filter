package com.focusfilter.di

import com.focusfilter.data.api.BackendApiService
import com.focusfilter.reasoning.BackendReasoningEngine
import com.focusfilter.reasoning.OnDeviceReasoningEngine
import com.focusfilter.reasoning.ReasoningEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

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
