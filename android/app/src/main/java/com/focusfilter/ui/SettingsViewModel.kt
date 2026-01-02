package com.focusfilter.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusfilter.data.SettingsRepository
import com.focusfilter.data.ThemeSetting
import com.focusfilter.data.export.NotificationExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val exporter: NotificationExporter,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _exportResult = MutableStateFlow<String?>(null)
    val exportResult: StateFlow<String?> = _exportResult.asStateFlow()

    val apiEndpoint: StateFlow<String> = settingsRepository.apiEndpoint
    val themeSetting: StateFlow<ThemeSetting> = settingsRepository.themeSetting

    fun setApiEndpoint(endpoint: String) {
        settingsRepository.setApiEndpoint(endpoint)
    }

    fun setThemeSetting(setting: ThemeSetting) {
        settingsRepository.setThemeSetting(setting)
    }

    /**
     * Triggers the notification export process.
     */
    fun exportNotifications() {
        viewModelScope.launch {
            _exportResult.value = exporter.exportToJson()
        }
    }

    /**
     * Resets the export result to null, so the share sheet is not re-triggered on configuration change.
     */
    fun onExportConsumed() {
        _exportResult.value = null
    }
}
