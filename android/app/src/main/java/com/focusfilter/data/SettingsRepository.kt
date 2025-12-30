package com.focusfilter.data

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _passthroughEnabled = MutableStateFlow(isPassthroughEnabled())
    val passthroughEnabled: StateFlow<Boolean> = _passthroughEnabled

    private val _apiEndpoint = MutableStateFlow(getApiEndpoint())
    val apiEndpoint: StateFlow<String> = _apiEndpoint

    private val _themeSetting = MutableStateFlow(getThemeSetting())
    val themeSetting: StateFlow<ThemeSetting> = _themeSetting

    private fun isPassthroughEnabled(): Boolean {
        return prefs.getBoolean(KEY_PASSTHROUGH_ENABLED, false)
    }

    fun setPassthroughEnabled(isEnabled: Boolean) {
        prefs.edit().putBoolean(KEY_PASSTHROUGH_ENABLED, isEnabled).apply()
        _passthroughEnabled.value = isEnabled
    }

    private fun getApiEndpoint(): String {
        return prefs.getString(KEY_API_ENDPOINT, "http://10.0.2.2:8000/") ?: "http://10.0.2.2:8000/"
    }

    fun setApiEndpoint(endpoint: String) {
        prefs.edit().putString(KEY_API_ENDPOINT, endpoint).apply()
        _apiEndpoint.value = endpoint
    }

    private fun getThemeSetting(): ThemeSetting {
        val settingName = prefs.getString(KEY_THEME_SETTING, ThemeSetting.SYSTEM.name)
        return ThemeSetting.valueOf(settingName ?: ThemeSetting.SYSTEM.name)
    }

    fun setThemeSetting(setting: ThemeSetting) {
        prefs.edit().putString(KEY_THEME_SETTING, setting.name).apply()
        _themeSetting.value = setting
    }

    companion object {
        private const val PREFS_NAME = "focus_filter_settings"
        private const val KEY_PASSTHROUGH_ENABLED = "passthrough_enabled"
        private const val KEY_API_ENDPOINT = "api_endpoint"
        private const val KEY_THEME_SETTING = "theme_setting"
    }
}
