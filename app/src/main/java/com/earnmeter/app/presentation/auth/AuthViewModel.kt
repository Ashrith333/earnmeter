package com.earnmeter.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.earnmeter.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthState(
    val isLoading: Boolean = false,
    val phoneNumber: String = "",
    val password: String = "",
    val isNewUser: Boolean? = null, // null means not checked yet
    val error: String? = null,
    val otpSent: Boolean = false,
    val isAuthenticated: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()
    
    val isLoggedIn = authRepository.isLoggedIn
    
    fun onPhoneNumberChanged(phoneNumber: String) {
        _state.value = _state.value.copy(phoneNumber = phoneNumber, error = null)
    }
    
    fun onPasswordChanged(password: String) {
        _state.value = _state.value.copy(password = password, error = null)
    }
    
    fun checkUserExists() {
        val phoneNumber = _state.value.phoneNumber
        if (phoneNumber.length < 10) {
            _state.value = _state.value.copy(error = "Please enter a valid phone number")
            return
        }
        
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            val formattedPhone = formatPhoneNumber(phoneNumber)
            val result = authRepository.checkUserExists(formattedPhone)
            
            result.fold(
                onSuccess = { exists ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isNewUser = !exists,
                        phoneNumber = formattedPhone
                    )
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to check user"
                    )
                }
            )
        }
    }
    
    fun signIn() {
        val phoneNumber = _state.value.phoneNumber
        val password = _state.value.password
        
        if (password.length < 6) {
            _state.value = _state.value.copy(error = "Password must be at least 6 characters")
            return
        }
        
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            val result = authRepository.signIn(phoneNumber, password)
            
            result.fold(
                onSuccess = {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isAuthenticated = true
                    )
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message ?: "Sign in failed"
                    )
                }
            )
        }
    }
    
    fun signUp() {
        val phoneNumber = _state.value.phoneNumber
        val password = _state.value.password
        
        if (password.length < 6) {
            _state.value = _state.value.copy(error = "Password must be at least 6 characters")
            return
        }
        
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            val result = authRepository.signUp(phoneNumber, password)
            
            result.fold(
                onSuccess = {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        otpSent = true
                    )
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message ?: "Sign up failed"
                    )
                }
            )
        }
    }
    
    fun verifyOtp(otp: String) {
        if (otp.length != 6) {
            _state.value = _state.value.copy(error = "Please enter a valid 6-digit OTP")
            return
        }
        
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            val result = authRepository.verifyOtp(_state.value.phoneNumber, otp)
            
            result.fold(
                onSuccess = {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isAuthenticated = true
                    )
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message ?: "OTP verification failed"
                    )
                }
            )
        }
    }
    
    fun resendOtp() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            val result = authRepository.resendOtp(_state.value.phoneNumber)
            
            result.fold(
                onSuccess = {
                    _state.value = _state.value.copy(isLoading = false)
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to resend OTP"
                    )
                }
            )
        }
    }
    
    fun forgotPassword() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            val result = authRepository.resetPassword(_state.value.phoneNumber)
            
            result.fold(
                onSuccess = {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        otpSent = true
                    )
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to send reset code"
                    )
                }
            )
        }
    }
    
    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _state.value = AuthState()
        }
    }
    
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
    
    fun resetState() {
        _state.value = AuthState()
    }
    
    private fun formatPhoneNumber(phone: String): String {
        val cleaned = phone.replace(Regex("[^0-9]"), "")
        return if (cleaned.startsWith("91") && cleaned.length == 12) {
            "+$cleaned"
        } else if (cleaned.length == 10) {
            "+91$cleaned"
        } else {
            "+$cleaned"
        }
    }
}

