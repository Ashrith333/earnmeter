package com.earnmeter.app.data.repository

import com.earnmeter.app.data.local.dao.UserSettingsDao
import com.earnmeter.app.data.local.entity.UserSettingsEntity
import com.earnmeter.app.data.remote.SupabaseClient
import com.earnmeter.app.domain.model.AdminSuggestedRanges
import com.earnmeter.app.domain.model.SupportedApp
import com.earnmeter.app.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val settingsDao: UserSettingsDao,
    private val supabaseClient: SupabaseClient
) : SettingsRepository {
    
    override fun observeSettings(userId: String): Flow<UserSettings?> {
        return settingsDao.observeSettings(userId).map { entity ->
            entity?.toDomainModel()
        }
    }
    
    override suspend fun getSettings(userId: String): Result<UserSettings?> {
        return try {
            // Try to get from server first
            val serverResult = supabaseClient.getUserSettings(userId)
            if (serverResult.isSuccess) {
                val settings = serverResult.getOrNull()
                if (settings != null) {
                    // Cache locally
                    settingsDao.insert(UserSettingsEntity.fromDomainModel(settings))
                }
                Result.success(settings)
            } else {
                // Fallback to local cache
                val localSettings = settingsDao.getSettings(userId)
                Result.success(localSettings?.toDomainModel())
            }
        } catch (e: Exception) {
            // Fallback to local cache
            try {
                val localSettings = settingsDao.getSettings(userId)
                Result.success(localSettings?.toDomainModel())
            } catch (e2: Exception) {
                Result.failure(e2)
            }
        }
    }
    
    override suspend fun updateSettings(settings: UserSettings): Result<Unit> {
        return try {
            // Update locally first
            settingsDao.insert(UserSettingsEntity.fromDomainModel(settings))
            
            // Sync with server
            supabaseClient.updateUserSettings(settings)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getSuggestedRanges(city: String): Result<AdminSuggestedRanges?> {
        return supabaseClient.getSuggestedRanges(city)
    }
    
    override suspend fun getSupportedApps(): Result<List<SupportedApp>> {
        return supabaseClient.getSupportedApps()
    }
    
    override suspend fun getFeatureName(featureKey: String): Result<String?> {
        return try {
            val configResult = supabaseClient.getAppConfig()
            if (configResult.isSuccess) {
                val configs = configResult.getOrNull() ?: emptyList()
                val featureName = configs.find { it.configKey == featureKey }?.configValue
                Result.success(featureName)
            } else {
                Result.failure(configResult.exceptionOrNull() ?: Exception("Failed to fetch config"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

