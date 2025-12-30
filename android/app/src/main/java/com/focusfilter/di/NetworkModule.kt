package com.focusfilter.di

import com.focusfilter.data.SettingsRepository
import com.focusfilter.data.api.BackendApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        settingsRepository: SettingsRepository
    ): Retrofit {
        // This is a bit of a workaround to get the initial value from the StateFlow
        // In a real app, you might have a more sophisticated way to handle dynamic base URLs
        val initialBaseUrl = runBlocking {
            settingsRepository.apiEndpoint.first()
        }

        return Retrofit.Builder()
            .baseUrl(initialBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideBackendApiService(retrofit: Retrofit): BackendApiService {
        return retrofit.create(BackendApiService::class.java)
    }
}
