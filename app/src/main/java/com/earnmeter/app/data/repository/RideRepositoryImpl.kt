package com.earnmeter.app.data.repository

import com.earnmeter.app.data.local.dao.RideDao
import com.earnmeter.app.data.local.entity.RideEntity
import com.earnmeter.app.data.remote.SupabaseClient
import com.earnmeter.app.domain.model.Ride
import com.earnmeter.app.domain.model.RideAction
import com.earnmeter.app.domain.model.RideAnalytics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RideRepositoryImpl @Inject constructor(
    private val rideDao: RideDao,
    private val supabaseClient: SupabaseClient
) : RideRepository {
    
    override fun observeRides(userId: String): Flow<List<Ride>> {
        return rideDao.getRidesByUser(userId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override suspend fun insertRide(ride: Ride): Result<Ride> {
        return try {
            // First insert locally
            val entity = RideEntity.fromDomainModel(ride, isSynced = false)
            rideDao.insert(entity)
            
            // Try to sync with server
            val serverResult = supabaseClient.insertRide(ride)
            if (serverResult.isSuccess) {
                rideDao.markAsSynced(entity.id)
                serverResult
            } else {
                // Return local ride if server sync fails
                Result.success(entity.toDomainModel())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateRide(ride: Ride): Result<Unit> {
        return try {
            val entity = RideEntity.fromDomainModel(ride, isSynced = false)
            rideDao.update(entity)
            
            // Try to sync with server
            val serverResult = supabaseClient.updateRide(ride)
            if (serverResult.isSuccess) {
                rideDao.markAsSynced(entity.id)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateRideAction(rideId: String, action: RideAction): Result<Unit> {
        return try {
            val entity = rideDao.getRideById(rideId) ?: return Result.failure(
                Exception("Ride not found")
            )
            val updatedEntity = entity.copy(
                action = action,
                actionTimestamp = Instant.now().toString(),
                isSynced = false
            )
            rideDao.update(updatedEntity)
            
            // Try to sync with server
            val ride = updatedEntity.toDomainModel()
            val serverResult = supabaseClient.updateRide(ride)
            if (serverResult.isSuccess) {
                rideDao.markAsSynced(rideId)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getRides(userId: String, limit: Int, offset: Int): Result<List<Ride>> {
        return try {
            // Try to get from server first
            val serverResult = supabaseClient.getRides(userId, limit, offset)
            if (serverResult.isSuccess) {
                val rides = serverResult.getOrNull() ?: emptyList()
                // Cache locally
                rides.forEach { ride ->
                    rideDao.insert(RideEntity.fromDomainModel(ride, isSynced = true))
                }
                Result.success(rides)
            } else {
                // Fallback to local cache
                val localRides = rideDao.getRidesByUserPaginated(userId, limit, offset)
                Result.success(localRides.map { it.toDomainModel() })
            }
        } catch (e: Exception) {
            // Fallback to local cache
            try {
                val localRides = rideDao.getRidesByUserPaginated(userId, limit, offset)
                Result.success(localRides.map { it.toDomainModel() })
            } catch (e2: Exception) {
                Result.failure(e2)
            }
        }
    }
    
    override suspend fun getRidesByDate(
        userId: String,
        startDate: String,
        endDate: String
    ): Result<List<Ride>> {
        return try {
            val serverResult = supabaseClient.getRidesByDate(userId, startDate, endDate)
            if (serverResult.isSuccess) {
                serverResult
            } else {
                val localRides = rideDao.getRidesByDateRange(userId, startDate, endDate)
                Result.success(localRides.map { it.toDomainModel() })
            }
        } catch (e: Exception) {
            try {
                val localRides = rideDao.getRidesByDateRange(userId, startDate, endDate)
                Result.success(localRides.map { it.toDomainModel() })
            } catch (e2: Exception) {
                Result.failure(e2)
            }
        }
    }
    
    override suspend fun getRidesByAction(userId: String, action: RideAction): Result<List<Ride>> {
        return try {
            val localRides = rideDao.getRidesByAction(userId, action)
            Result.success(localRides.map { it.toDomainModel() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getTodayStats(userId: String): Result<TodayStats> {
        return try {
            val rideCount = rideDao.getTodayRideCount(userId)
            val earnings = rideDao.getTodayEarnings(userId) ?: 0.0
            val distance = rideDao.getTodayDistance(userId) ?: 0.0
            
            val acceptedRides = rideDao.getRidesByAction(userId, RideAction.ACCEPTED)
            val rejectedRides = rideDao.getRidesByAction(userId, RideAction.REJECTED)
            
            Result.success(
                TodayStats(
                    rideCount = rideCount,
                    earnings = earnings,
                    distance = distance,
                    acceptedCount = acceptedRides.size,
                    rejectedCount = rejectedRides.size
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getAnalytics(userId: String, date: String): Result<RideAnalytics?> {
        return supabaseClient.getAnalytics(userId, date)
    }
    
    override suspend fun getAnalyticsRange(
        userId: String,
        startDate: String,
        endDate: String
    ): Result<List<RideAnalytics>> {
        return supabaseClient.getAnalyticsRange(userId, startDate, endDate)
    }
    
    override suspend fun syncPendingRides(): Result<Int> {
        return try {
            val unsyncedRides = rideDao.getUnsyncedRides()
            var syncedCount = 0
            
            unsyncedRides.forEach { entity ->
                val ride = entity.toDomainModel()
                val result = if (ride.id == null) {
                    supabaseClient.insertRide(ride)
                } else {
                    supabaseClient.updateRide(ride).map { ride }
                }
                
                if (result.isSuccess) {
                    rideDao.markAsSynced(entity.id)
                    syncedCount++
                }
            }
            
            Result.success(syncedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

