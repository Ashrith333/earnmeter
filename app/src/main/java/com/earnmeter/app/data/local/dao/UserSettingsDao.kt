package com.earnmeter.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.earnmeter.app.data.local.entity.UserSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSettingsDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(settings: UserSettingsEntity)
    
    @Update
    suspend fun update(settings: UserSettingsEntity)
    
    @Query("SELECT * FROM user_settings WHERE user_id = :userId")
    suspend fun getSettings(userId: String): UserSettingsEntity?
    
    @Query("SELECT * FROM user_settings WHERE user_id = :userId")
    fun observeSettings(userId: String): Flow<UserSettingsEntity?>
    
    @Query("DELETE FROM user_settings WHERE user_id = :userId")
    suspend fun delete(userId: String)
    
    @Query("DELETE FROM user_settings")
    suspend fun deleteAll()
}

