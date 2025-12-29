package com.focusfilter

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for Focus Filter.
 * Initializes Hilt dependency injection.
 */
@HiltAndroidApp
class FocusFilterApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
