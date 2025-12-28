package com.earnmeter.app.data.repository

import com.earnmeter.app.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val isLoggedIn: Flow<Boolean>
    val currentUserId: String?
    
    suspend fun checkUserExists(phoneNumber: String): Result<Boolean>
    suspend fun signUp(phoneNumber: String, password: String): Result<Unit>
    suspend fun signIn(phoneNumber: String, password: String): Result<Unit>
    suspend fun verifyOtp(phoneNumber: String, otp: String): Result<Unit>
    suspend fun resendOtp(phoneNumber: String): Result<Unit>
    suspend fun resetPassword(phoneNumber: String): Result<Unit>
    suspend fun updatePassword(newPassword: String): Result<Unit>
    suspend fun signOut(): Result<Unit>
    suspend fun getCurrentUser(): Result<User?>
    suspend fun updateUser(user: User): Result<Unit>
}

