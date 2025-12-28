package com.earnmeter.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.earnmeter.app.data.local.entity.RideEntity
import com.earnmeter.app.domain.model.RideAction
import kotlinx.coroutines.flow.Flow

@Dao
interface RideDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ride: RideEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rides: List<RideEntity>)
    
    @Update
    suspend fun update(ride: RideEntity)
    
    @Query("SELECT * FROM rides WHERE id = :id")
    suspend fun getRideById(id: String): RideEntity?
    
    @Query("SELECT * FROM rides WHERE user_id = :userId ORDER BY created_at DESC")
    fun getRidesByUser(userId: String): Flow<List<RideEntity>>
    
    @Query("SELECT * FROM rides WHERE user_id = :userId ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    suspend fun getRidesByUserPaginated(userId: String, limit: Int, offset: Int): List<RideEntity>
    
    @Query("""
        SELECT * FROM rides 
        WHERE user_id = :userId 
        AND created_at >= :startDate 
        AND created_at <= :endDate 
        ORDER BY created_at DESC
    """)
    suspend fun getRidesByDateRange(userId: String, startDate: String, endDate: String): List<RideEntity>
    
    @Query("SELECT * FROM rides WHERE user_id = :userId AND action = :action ORDER BY created_at DESC")
    suspend fun getRidesByAction(userId: String, action: RideAction): List<RideEntity>
    
    @Query("SELECT * FROM rides WHERE is_synced = 0")
    suspend fun getUnsyncedRides(): List<RideEntity>
    
    @Query("UPDATE rides SET is_synced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)
    
    @Query("UPDATE rides SET is_synced = 1 WHERE id IN (:ids)")
    suspend fun markAllAsSynced(ids: List<String>)
    
    @Query("""
        SELECT COUNT(*) FROM rides 
        WHERE user_id = :userId 
        AND date(created_at) = date('now')
    """)
    suspend fun getTodayRideCount(userId: String): Int
    
    @Query("""
        SELECT SUM(fare_amount) FROM rides 
        WHERE user_id = :userId 
        AND action = 'ACCEPTED'
        AND date(created_at) = date('now')
    """)
    suspend fun getTodayEarnings(userId: String): Double?
    
    @Query("""
        SELECT SUM(distance_km) FROM rides 
        WHERE user_id = :userId 
        AND action = 'ACCEPTED'
        AND date(created_at) = date('now')
    """)
    suspend fun getTodayDistance(userId: String): Double?
    
    @Query("DELETE FROM rides WHERE user_id = :userId")
    suspend fun deleteAllByUser(userId: String)
    
    @Query("DELETE FROM rides")
    suspend fun deleteAll()
}

