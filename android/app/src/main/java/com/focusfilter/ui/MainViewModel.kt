package com.focusfilter.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusfilter.data.NotificationRepository
import com.focusfilter.data.SettingsRepository
import com.focusfilter.data.ThemeSetting
import com.focusfilter.data.room.NotificationEntity
import com.focusfilter.models.NotificationCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val notifications: StateFlow<List<NotificationEntity>> = notificationRepository.getAllNotifications()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val isPassthroughEnabled: StateFlow<Boolean> = settingsRepository.passthroughEnabled
    
    val themeSetting: StateFlow<ThemeSetting> = settingsRepository.themeSetting

    fun updateUserClassification(notificationId: Long, category: NotificationCategory) {
        viewModelScope.launch {
            notificationRepository.updateUserClassification(notificationId, category)
        }
    }

    fun updateActionable(notificationId: Long, isActionable: Boolean) {
        viewModelScope.launch {
            notificationRepository.updateActionable(notificationId, isActionable)
        }
    }

    fun setPassthroughEnabled(isEnabled: Boolean) {
        settingsRepository.setPassthroughEnabled(isEnabled)
    }
}
