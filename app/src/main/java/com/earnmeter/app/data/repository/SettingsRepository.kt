package com.earnmeter.app.data.repository

import com.earnmeter.app.domain.model.AdminSuggestedRanges
import com.earnmeter.app.domain.model.SupportedApp
import com.earnmeter.app.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeSettings(userId: String): Flow<UserSettings?>
    
    suspend fun getSettings(userId: String): Result<UserSettings?>
    suspend fun updateSettings(settings: UserSettings): Result<Unit>
    
    suspend fun getSuggestedRanges(city: String): Result<AdminSuggestedRanges?>
    suspend fun getSupportedApps(): Result<List<SupportedApp>>
    
    suspend fun getFeatureName(featureKey: String): Result<String?>
}

