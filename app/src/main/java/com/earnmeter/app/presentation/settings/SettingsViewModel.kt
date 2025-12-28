package com.earnmeter.app.presentation.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.earnmeter.app.BuildConfig
import com.earnmeter.app.data.repository.AuthRepository
import com.earnmeter.app.data.repository.RideRepository
import com.earnmeter.app.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class SettingsState(
    val isLoading: Boolean = false,
    val smartAssistName: String = BuildConfig.FEATURE_SMART_ASSIST_NAME,
    val trackProfitsName: String = BuildConfig.FEATURE_TRACK_PROFITS_NAME,
    val rideAlertsEnabled: Boolean = true,
    val dailySummaryEnabled: Boolean = true,
    val lastSyncTime: String = "Never",
    val showClearDataDialog: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository,
    private val rideRepository: RideRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            // Load feature names
            val smartAssistResult = settingsRepository.getFeatureName("feature_smart_assist_name")
            val trackProfitsResult = settingsRepository.getFeatureName("feature_track_profits_name")
            
            _state.value = _state.value.copy(
                smartAssistName = smartAssistResult.getOrNull() ?: BuildConfig.FEATURE_SMART_ASSIST_NAME,
                trackProfitsName = trackProfitsResult.getOrNull() ?: BuildConfig.FEATURE_TRACK_PROFITS_NAME
            )
        }
    }
    
    fun toggleRideAlerts(enabled: Boolean) {
        _state.value = _state.value.copy(rideAlertsEnabled = enabled)
        // TODO: Save to preferences
    }
    
    fun toggleDailySummary(enabled: Boolean) {
        _state.value = _state.value.copy(dailySummaryEnabled = enabled)
        // TODO: Save to preferences and schedule/cancel daily summary worker
    }
    
    fun syncData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            val result = rideRepository.syncPendingRides()
            val now = Instant.now()
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("MMM d, h:mm a"))
            
            _state.value = _state.value.copy(
                isLoading = false,
                lastSyncTime = now,
                message = if (result.isSuccess) 
                    "Synced ${result.getOrNull()} rides" 
                else 
                    "Sync failed"
            )
        }
    }
    
    fun showClearDataDialog() {
        _state.value = _state.value.copy(showClearDataDialog = true)
    }
    
    fun hideClearDataDialog() {
        _state.value = _state.value.copy(showClearDataDialog = false)
    }
    
    fun clearLocalData() {
        viewModelScope.launch {
            // TODO: Clear local database
            _state.value = _state.value.copy(
                showClearDataDialog = false,
                message = "Local data cleared"
            )
        }
    }
    
    fun openTerms() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://earnmeter.app/terms")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
    
    fun openPrivacy() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://earnmeter.app/privacy")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}

