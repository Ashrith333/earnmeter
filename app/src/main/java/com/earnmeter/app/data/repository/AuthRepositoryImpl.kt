package com.earnmeter.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.earnmeter.app.data.remote.SupabaseClient
import com.earnmeter.app.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val dataStore: DataStore<Preferences>
) : AuthRepository {
    
    companion object {
        private val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_PHONE_NUMBER = stringPreferencesKey("phone_number")
    }
    
    override val isLoggedIn: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_IS_LOGGED_IN] ?: false
    }
    
    override val currentUserId: String?
        get() = supabaseClient.getCurrentUserId()
    
    override suspend fun checkUserExists(phoneNumber: String): Result<Boolean> {
        return supabaseClient.checkUserExists(phoneNumber)
    }
    
    override suspend fun signUp(phoneNumber: String, password: String): Result<Unit> {
        val result = supabaseClient.signUpWithPhone(phoneNumber, password)
        if (result.isSuccess) {
            savePhoneNumber(phoneNumber)
        }
        return result
    }
    
    override suspend fun signIn(phoneNumber: String, password: String): Result<Unit> {
        val result = supabaseClient.signInWithPhone(phoneNumber, password)
        if (result.isSuccess) {
            saveLoginState(true, phoneNumber)
        }
        return result
    }
    
    override suspend fun verifyOtp(phoneNumber: String, otp: String): Result<Unit> {
        val result = supabaseClient.verifyOtp(phoneNumber, otp)
        if (result.isSuccess) {
            saveLoginState(true, phoneNumber)
        }
        return result
    }
    
    override suspend fun resendOtp(phoneNumber: String): Result<Unit> {
        return supabaseClient.resendOtp(phoneNumber)
    }
    
    override suspend fun resetPassword(phoneNumber: String): Result<Unit> {
        return supabaseClient.resetPassword(phoneNumber)
    }
    
    override suspend fun updatePassword(newPassword: String): Result<Unit> {
        return supabaseClient.updatePassword(newPassword)
    }
    
    override suspend fun signOut(): Result<Unit> {
        val result = supabaseClient.signOut()
        if (result.isSuccess) {
            clearLoginState()
        }
        return result
    }
    
    override suspend fun getCurrentUser(): Result<User?> {
        val userId = currentUserId ?: return Result.success(null)
        return supabaseClient.getUser(userId)
    }
    
    override suspend fun updateUser(user: User): Result<Unit> {
        return supabaseClient.updateUser(user)
    }
    
    private suspend fun saveLoginState(isLoggedIn: Boolean, phoneNumber: String) {
        dataStore.edit { prefs ->
            prefs[KEY_IS_LOGGED_IN] = isLoggedIn
            prefs[KEY_PHONE_NUMBER] = phoneNumber
            supabaseClient.getCurrentUserId()?.let { userId ->
                prefs[KEY_USER_ID] = userId
            }
        }
    }
    
    private suspend fun savePhoneNumber(phoneNumber: String) {
        dataStore.edit { prefs ->
            prefs[KEY_PHONE_NUMBER] = phoneNumber
        }
    }
    
    private suspend fun clearLoginState() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_IS_LOGGED_IN)
            prefs.remove(KEY_USER_ID)
            prefs.remove(KEY_PHONE_NUMBER)
        }
    }
}

