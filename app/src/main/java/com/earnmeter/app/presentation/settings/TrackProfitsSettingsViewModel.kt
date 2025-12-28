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

data class TrackProfitsSettingsState(
    val isLoading: Boolean = true,
    
    // Earnings per KM
    val goodEarningsPerKm: Double = 15.0,
    val avgEarningsPerKm: Double = 10.0,
    val badEarningsPerKm: Double = 5.0,
    
    // Earnings per Hour
    val goodEarningsPerHour: Double = 300.0,
    val avgEarningsPerHour: Double = 200.0,
    val badEarningsPerHour: Double = 100.0,
    
    // User Rating
    val goodRating: Double = 4.5,
    val avgRating: Double = 4.0,
    val badRating: Double = 3.5,
    
    val saved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TrackProfitsSettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(TrackProfitsSettingsState())
    val state: StateFlow<TrackProfitsSettingsState> = _state.asStateFlow()
    
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
                    goodEarningsPerKm = settings.goodEarningsPerKm,
                    avgEarningsPerKm = settings.avgEarningsPerKm,
                    badEarningsPerKm = settings.badEarningsPerKm,
                    goodEarningsPerHour = settings.goodEarningsPerHour,
                    avgEarningsPerHour = settings.avgEarningsPerHour,
                    badEarningsPerHour = settings.badEarningsPerHour,
                    goodRating = settings.goodRating,
                    avgRating = settings.avgRating,
                    badRating = settings.badRating
                )
            } ?: run {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }
    
    fun updateGoodEarningsPerKm(value: Double) {
        _state.value = _state.value.copy(
            goodEarningsPerKm = value,
            saved = false,
            error = null
        )
    }
    
    fun updateAvgEarningsPerKm(value: Double) {
        _state.value = _state.value.copy(
            avgEarningsPerKm = value,
            saved = false,
            error = null
        )
    }
    
    fun updateBadEarningsPerKm(value: Double) {
        _state.value = _state.value.copy(
            badEarningsPerKm = value,
            saved = false,
            error = null
        )
    }
    
    fun updateGoodEarningsPerHour(value: Double) {
        _state.value = _state.value.copy(
            goodEarningsPerHour = value,
            saved = false,
            error = null
        )
    }
    
    fun updateAvgEarningsPerHour(value: Double) {
        _state.value = _state.value.copy(
            avgEarningsPerHour = value,
            saved = false,
            error = null
        )
    }
    
    fun updateBadEarningsPerHour(value: Double) {
        _state.value = _state.value.copy(
            badEarningsPerHour = value,
            saved = false,
            error = null
        )
    }
    
    fun updateGoodRating(value: Double) {
        _state.value = _state.value.copy(
            goodRating = value,
            saved = false,
            error = null
        )
    }
    
    fun updateAvgRating(value: Double) {
        _state.value = _state.value.copy(
            avgRating = value,
            saved = false,
            error = null
        )
    }
    
    fun updateBadRating(value: Double) {
        _state.value = _state.value.copy(
            badRating = value,
            saved = false,
            error = null
        )
    }
    
    fun saveSettings() {
        viewModelScope.launch {
            val userId = authRepository.currentUserId ?: return@launch
            
            // Validate ranges
            val currentState = _state.value
            if (currentState.badEarningsPerKm >= currentState.avgEarningsPerKm ||
                currentState.avgEarningsPerKm >= currentState.goodEarningsPerKm) {
                _state.value = _state.value.copy(
                    error = "Bad < Average < Good for Earnings per KM"
                )
                return@launch
            }
            
            if (currentState.badEarningsPerHour >= currentState.avgEarningsPerHour ||
                currentState.avgEarningsPerHour >= currentState.goodEarningsPerHour) {
                _state.value = _state.value.copy(
                    error = "Bad < Average < Good for Earnings per Hour"
                )
                return@launch
            }
            
            if (currentState.badRating >= currentState.avgRating ||
                currentState.avgRating >= currentState.goodRating) {
                _state.value = _state.value.copy(
                    error = "Bad < Average < Good for Ratings"
                )
                return@launch
            }
            
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            val updatedSettings = (originalSettings ?: UserSettings(userId = userId)).copy(
                goodEarningsPerKm = currentState.goodEarningsPerKm,
                avgEarningsPerKm = currentState.avgEarningsPerKm,
                badEarningsPerKm = currentState.badEarningsPerKm,
                goodEarningsPerHour = currentState.goodEarningsPerHour,
                avgEarningsPerHour = currentState.avgEarningsPerHour,
                badEarningsPerHour = currentState.badEarningsPerHour,
                goodRating = currentState.goodRating,
                avgRating = currentState.avgRating,
                badRating = currentState.badRating
            )
            
            val result = settingsRepository.updateSettings(updatedSettings)
            
            if (result.isSuccess) {
                originalSettings = updatedSettings
                _state.value = _state.value.copy(isLoading = false, saved = true)
                
                delay(3000)
                _state.value = _state.value.copy(saved = false)
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Failed to save settings"
                )
            }
        }
    }
    
    fun suggestRanges() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            // Get user's city from profile and fetch suggested ranges
            val user = authRepository.getCurrentUser().getOrNull()
            val city = user?.city
            
            if (city.isNullOrBlank()) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Please set your city in Profile to get suggestions"
                )
                return@launch
            }
            
            val suggestedRanges = settingsRepository.getSuggestedRanges(city)
            
            suggestedRanges.getOrNull()?.let { ranges ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    goodEarningsPerKm = ranges.suggestedGoodPerKm,
                    avgEarningsPerKm = ranges.suggestedAvgPerKm,
                    badEarningsPerKm = ranges.suggestedBadPerKm,
                    goodEarningsPerHour = ranges.suggestedGoodPerHour,
                    avgEarningsPerHour = ranges.suggestedAvgPerHour,
                    badEarningsPerHour = ranges.suggestedBadPerHour,
                    goodRating = ranges.suggestedGoodRating,
                    avgRating = ranges.suggestedAvgRating,
                    badRating = ranges.suggestedBadRating,
                    saved = false
                )
            } ?: run {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "No suggestions available for $city yet"
                )
            }
        }
    }
}

