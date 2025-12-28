package com.earnmeter.app.data.repository

import com.earnmeter.app.domain.model.Ride
import com.earnmeter.app.domain.model.RideAction
import com.earnmeter.app.domain.model.RideAnalytics
import kotlinx.coroutines.flow.Flow

interface RideRepository {
    fun observeRides(userId: String): Flow<List<Ride>>
    
    suspend fun insertRide(ride: Ride): Result<Ride>
    suspend fun updateRide(ride: Ride): Result<Unit>
    suspend fun updateRideAction(rideId: String, action: RideAction): Result<Unit>
    
    suspend fun getRides(userId: String, limit: Int = 50, offset: Int = 0): Result<List<Ride>>
    suspend fun getRidesByDate(userId: String, startDate: String, endDate: String): Result<List<Ride>>
    suspend fun getRidesByAction(userId: String, action: RideAction): Result<List<Ride>>
    
    suspend fun getTodayStats(userId: String): Result<TodayStats>
    
    suspend fun getAnalytics(userId: String, date: String): Result<RideAnalytics?>
    suspend fun getAnalyticsRange(userId: String, startDate: String, endDate: String): Result<List<RideAnalytics>>
    
    suspend fun syncPendingRides(): Result<Int>
}

data class TodayStats(
    val rideCount: Int = 0,
    val earnings: Double = 0.0,
    val distance: Double = 0.0,
    val acceptedCount: Int = 0,
    val rejectedCount: Int = 0
)

