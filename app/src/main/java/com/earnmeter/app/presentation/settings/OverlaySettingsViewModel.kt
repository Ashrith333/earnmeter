package com.earnmeter.app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.earnmeter.app.data.repository.AuthRepository
import com.earnmeter.app.data.repository.SettingsRepository
import com.earnmeter.app.domain.model.UserSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OverlaySettingsState(
    val isLoading: Boolean = true,
    val fontSize: Int = 14,
    val durationMs: Long = 5000,
    val opacity: Float = 0.9f,
    val position: String = "TOP_RIGHT",
    val saved: Boolean = false
)

@HiltViewModel
class OverlaySettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(OverlaySettingsState())
    val state: StateFlow<OverlaySettingsState> = _state.asStateFlow()
    
    private var originalSettings: UserSettings? = null
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            val userId = authRepository.currentUserId ?: return@launch
            
            val result = settingsRepository.getSettings(userId)
            result.getOrNull()?.let { settings ->
                originalSettings = settings
                _state.value = _state.value.copy(
                    isLoading = false,
                    fontSize = settings.overlayFontSize,
                    durationMs = settings.overlayDurationMs,
                    opacity = settings.overlayOpacity,
                    position = settings.overlayPosition
                )
            } ?: run {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }
    
    fun updateFontSize(size: Int) {
        _state.value = _state.value.copy(fontSize = size, saved = false)
    }
    
    fun updateDuration(durationMs: Long) {
        _state.value = _state.value.copy(durationMs = durationMs, saved = false)
    }
    
    fun updateOpacity(opacity: Float) {
        _state.value = _state.value.copy(opacity = opacity, saved = false)
    }
    
    fun updatePosition(position: String) {
        _state.value = _state.value.copy(position = position, saved = false)
    }
    
    fun saveSettings() {
        viewModelScope.launch {
            val userId = authRepository.currentUserId ?: return@launch
            
            _state.value = _state.value.copy(isLoading = true)
            
            val updatedSettings = (originalSettings ?: UserSettings(userId = userId)).copy(
                overlayFontSize = _state.value.fontSize,
                overlayDurationMs = _state.value.durationMs,
                overlayOpacity = _state.value.opacity,
                overlayPosition = _state.value.position
            )
            
            val result = settingsRepository.updateSettings(updatedSettings)
            
            if (result.isSuccess) {
                originalSettings = updatedSettings
                _state.value = _state.value.copy(isLoading = false, saved = true)
                
                // Hide the saved message after a delay
                delay(3000)
                _state.value = _state.value.copy(saved = false)
            } else {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }
}

