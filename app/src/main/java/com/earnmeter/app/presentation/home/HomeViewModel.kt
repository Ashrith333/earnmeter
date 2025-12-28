package com.earnmeter.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.earnmeter.app.BuildConfig
import com.earnmeter.app.data.repository.AuthRepository
import com.earnmeter.app.data.repository.RideRepository
import com.earnmeter.app.data.repository.SettingsRepository
import com.earnmeter.app.data.repository.TodayStats
import com.earnmeter.app.domain.model.Ride
import com.earnmeter.app.domain.model.UserSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeState(
    val isLoading: Boolean = true,
    val smartAssistName: String = BuildConfig.FEATURE_SMART_ASSIST_NAME,
    val trackProfitsName: String = BuildConfig.FEATURE_TRACK_PROFITS_NAME,
    val smartAssistEnabled: Boolean = true,
    val trackProfitsEnabled: Boolean = true,
    val todayStats: TodayStats = TodayStats(),
    val recentRides: List<Ride> = emptyList(),
    val userSettings: UserSettings? = null,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val rideRepository: RideRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()
    
    init {
        loadData()
    }
    
    fun loadData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            val userId = authRepository.currentUserId
            if (userId == null) {
                _state.value = _state.value.copy(isLoading = false, error = "Not logged in")
                return@launch
            }
            
            // Load feature names from config
            loadFeatureNames()
            
            // Load user settings
            val settingsResult = settingsRepository.getSettings(userId)
            val settings = settingsResult.getOrNull()
            
            // Load today's stats
            val statsResult = rideRepository.getTodayStats(userId)
            val stats = statsResult.getOrNull() ?: TodayStats()
            
            // Load recent rides
            val ridesResult = rideRepository.getRides(userId, limit = 10)
            val rides = ridesResult.getOrNull() ?: emptyList()
            
            _state.value = _state.value.copy(
                isLoading = false,
                userSettings = settings,
                smartAssistEnabled = settings?.smartAssistEnabled ?: true,
                trackProfitsEnabled = settings?.trackProfitsEnabled ?: true,
                todayStats = stats,
                recentRides = rides
            )
        }
    }
    
    private suspend fun loadFeatureNames() {
        // Try to get names from server config (allows remote configuration)
        val smartAssistResult = settingsRepository.getFeatureName("feature_smart_assist_name")
        val trackProfitsResult = settingsRepository.getFeatureName("feature_track_profits_name")
        
        _state.value = _state.value.copy(
            smartAssistName = smartAssistResult.getOrNull() ?: BuildConfig.FEATURE_SMART_ASSIST_NAME,
            trackProfitsName = trackProfitsResult.getOrNull() ?: BuildConfig.FEATURE_TRACK_PROFITS_NAME
        )
    }
    
    fun toggleSmartAssist(enabled: Boolean) {
        viewModelScope.launch {
            val userId = authRepository.currentUserId ?: return@launch
            val currentSettings = _state.value.userSettings ?: UserSettings(userId = userId)
            val updatedSettings = currentSettings.copy(smartAssistEnabled = enabled)
            
            settingsRepository.updateSettings(updatedSettings)
            _state.value = _state.value.copy(
                smartAssistEnabled = enabled,
                userSettings = updatedSettings
            )
        }
    }
    
    fun toggleTrackProfits(enabled: Boolean) {
        viewModelScope.launch {
            val userId = authRepository.currentUserId ?: return@launch
            val currentSettings = _state.value.userSettings ?: UserSettings(userId = userId)
            val updatedSettings = currentSettings.copy(trackProfitsEnabled = enabled)
            
            settingsRepository.updateSettings(updatedSettings)
            _state.value = _state.value.copy(
                trackProfitsEnabled = enabled,
                userSettings = updatedSettings
            )
        }
    }
    
    fun refresh() {
        loadData()
    }
}

