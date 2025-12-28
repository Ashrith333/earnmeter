package com.earnmeter.app.presentation.rides

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.earnmeter.app.data.repository.AuthRepository
import com.earnmeter.app.data.repository.RideRepository
import com.earnmeter.app.domain.model.Ride
import com.earnmeter.app.domain.model.RideAction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RideHistoryState(
    val isLoading: Boolean = true,
    val rides: List<Ride> = emptyList(),
    val filteredRides: List<Ride> = emptyList(),
    val currentFilter: RideAction? = null,
    val totalEarnings: Double = 0.0,
    val totalDistance: Double = 0.0,
    val avgEarningsPerKm: Double = 0.0,
    val hasMore: Boolean = false,
    val currentPage: Int = 0,
    val error: String? = null
)

@HiltViewModel
class RideHistoryViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val rideRepository: RideRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(RideHistoryState())
    val state: StateFlow<RideHistoryState> = _state.asStateFlow()
    
    private val pageSize = 20
    
    init {
        loadRides()
    }
    
    private fun loadRides() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            val userId = authRepository.currentUserId
            if (userId == null) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Not logged in"
                )
                return@launch
            }
            
            val result = rideRepository.getRides(userId, limit = pageSize, offset = 0)
            
            result.fold(
                onSuccess = { rides ->
                    val acceptedRides = rides.filter { it.action == RideAction.ACCEPTED }
                    val totalEarnings = acceptedRides.sumOf { it.fareAmount }
                    val totalDistance = acceptedRides.sumOf { it.distanceKm ?: 0.0 }
                    val avgEpk = if (totalDistance > 0) totalEarnings / totalDistance else 0.0
                    
                    _state.value = _state.value.copy(
                        isLoading = false,
                        rides = rides,
                        filteredRides = rides,
                        totalEarnings = totalEarnings,
                        totalDistance = totalDistance,
                        avgEarningsPerKm = avgEpk,
                        hasMore = rides.size >= pageSize,
                        currentPage = 0
                    )
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            )
        }
    }
    
    fun loadMore() {
        val currentState = _state.value
        if (currentState.isLoading || !currentState.hasMore) return
        
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            val userId = authRepository.currentUserId ?: return@launch
            val nextPage = currentState.currentPage + 1
            val offset = nextPage * pageSize
            
            val result = rideRepository.getRides(userId, limit = pageSize, offset = offset)
            
            result.fold(
                onSuccess = { newRides ->
                    val allRides = currentState.rides + newRides
                    val filteredRides = currentState.currentFilter?.let { filter ->
                        allRides.filter { it.action == filter }
                    } ?: allRides
                    
                    val acceptedRides = allRides.filter { it.action == RideAction.ACCEPTED }
                    val totalEarnings = acceptedRides.sumOf { it.fareAmount }
                    val totalDistance = acceptedRides.sumOf { it.distanceKm ?: 0.0 }
                    val avgEpk = if (totalDistance > 0) totalEarnings / totalDistance else 0.0
                    
                    _state.value = _state.value.copy(
                        isLoading = false,
                        rides = allRides,
                        filteredRides = filteredRides,
                        totalEarnings = totalEarnings,
                        totalDistance = totalDistance,
                        avgEarningsPerKm = avgEpk,
                        hasMore = newRides.size >= pageSize,
                        currentPage = nextPage
                    )
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            )
        }
    }
    
    fun filterByAction(action: RideAction?) {
        val currentRides = _state.value.rides
        val filteredRides = action?.let { filter ->
            currentRides.filter { it.action == filter }
        } ?: currentRides
        
        val relevantRides = if (action == RideAction.ACCEPTED) {
            filteredRides
        } else {
            currentRides.filter { it.action == RideAction.ACCEPTED }
        }
        
        val totalEarnings = relevantRides.sumOf { it.fareAmount }
        val totalDistance = relevantRides.sumOf { it.distanceKm ?: 0.0 }
        val avgEpk = if (totalDistance > 0) totalEarnings / totalDistance else 0.0
        
        _state.value = _state.value.copy(
            currentFilter = action,
            filteredRides = filteredRides,
            totalEarnings = totalEarnings,
            totalDistance = totalDistance,
            avgEarningsPerKm = avgEpk
        )
    }
    
    fun refresh() {
        _state.value = _state.value.copy(
            rides = emptyList(),
            currentPage = 0,
            hasMore = false
        )
        loadRides()
    }
}

