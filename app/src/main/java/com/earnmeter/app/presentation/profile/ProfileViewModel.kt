package com.earnmeter.app.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.earnmeter.app.data.repository.AuthRepository
import com.earnmeter.app.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val fullName: String = "",
    val email: String = "",
    val city: String = "",
    val stateRegion: String = "",
    val message: String? = null,
    val isError: Boolean = false,
    val isLoggedOut: Boolean = false,
    val passwordError: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()
    
    init {
        loadProfile()
    }
    
    private fun loadProfile() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            val result = authRepository.getCurrentUser()
            result.fold(
                onSuccess = { user ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        user = user,
                        fullName = user?.fullName ?: "",
                        email = user?.email ?: "",
                        city = user?.city ?: "",
                        stateRegion = user?.state ?: ""
                    )
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        message = e.message,
                        isError = true
                    )
                }
            )
        }
    }
    
    fun updateFullName(name: String) {
        _state.value = _state.value.copy(fullName = name, message = null)
    }
    
    fun updateEmail(email: String) {
        _state.value = _state.value.copy(email = email, message = null)
    }
    
    fun updateCity(city: String) {
        _state.value = _state.value.copy(city = city, message = null)
    }
    
    fun updateState(state: String) {
        _state.value = _state.value.copy(stateRegion = state, message = null)
    }
    
    fun saveProfile() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, message = null)
            
            val currentUser = _state.value.user ?: return@launch
            val updatedUser = currentUser.copy(
                fullName = _state.value.fullName.ifBlank { null },
                email = _state.value.email.ifBlank { null },
                city = _state.value.city.ifBlank { null },
                state = _state.value.stateRegion.ifBlank { null }
            )
            
            val result = authRepository.updateUser(updatedUser)
            result.fold(
                onSuccess = {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        user = updatedUser,
                        message = "Profile updated successfully",
                        isError = false
                    )
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        message = e.message ?: "Failed to update profile",
                        isError = true
                    )
                }
            )
        }
    }
    
    fun updatePassword(oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, passwordError = null)
            
            // For now, just update the password directly
            // In production, you'd verify the old password first
            val result = authRepository.updatePassword(newPassword)
            result.fold(
                onSuccess = {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        message = "Password updated successfully",
                        isError = false
                    )
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        passwordError = e.message ?: "Failed to update password"
                    )
                }
            )
        }
    }
    
    fun requestPasswordResetOtp() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, passwordError = null)
            
            val phoneNumber = _state.value.user?.phoneNumber ?: return@launch
            val result = authRepository.resetPassword(phoneNumber)
            result.fold(
                onSuccess = {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        message = "OTP sent to your phone",
                        isError = false
                    )
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        passwordError = e.message ?: "Failed to send OTP"
                    )
                }
            )
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            authRepository.signOut()
            _state.value = _state.value.copy(isLoggedOut = true)
        }
    }
    
    fun clearMessage() {
        _state.value = _state.value.copy(message = null, isError = false)
    }
}

