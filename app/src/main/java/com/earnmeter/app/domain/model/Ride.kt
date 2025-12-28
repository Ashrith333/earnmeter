package com.earnmeter.app.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Ride(
    val id: String? = null,
    @SerialName("user_id")
    val userId: String,
    
    // Source app (e.g., "uber", "ola", "rapido")
    @SerialName("source_app")
    val sourceApp: String,
    
    // Ride details
    @SerialName("pickup_location")
    val pickupLocation: String? = null,
    @SerialName("dropoff_location")
    val dropoffLocation: String? = null,
    @SerialName("distance_km")
    val distanceKm: Double? = null,
    @SerialName("estimated_duration_mins")
    val estimatedDurationMins: Int? = null,
    
    // Earnings
    @SerialName("fare_amount")
    val fareAmount: Double,
    @SerialName("surge_multiplier")
    val surgeMultiplier: Double? = null,
    @SerialName("tip_amount")
    val tipAmount: Double? = null,
    
    // Calculated metrics
    @SerialName("earnings_per_km")
    val earningsPerKm: Double? = null,
    @SerialName("earnings_per_hour")
    val earningsPerHour: Double? = null,
    
    // User rating (from notification if available)
    @SerialName("rider_rating")
    val riderRating: Double? = null,
    
    // Decision
    val action: RideAction = RideAction.PENDING,
    @SerialName("action_timestamp")
    val actionTimestamp: String? = null,
    
    // Classification based on user settings
    val classification: RideClassification = RideClassification.UNKNOWN,
    
    // Timestamps
    @SerialName("notification_received_at")
    val notificationReceivedAt: String,
    @SerialName("created_at")
    val createdAt: String? = null,
    
    // Raw notification data for debugging
    @SerialName("raw_notification_data")
    val rawNotificationData: String? = null
)

@Serializable
enum class RideAction {
    @SerialName("pending")
    PENDING,
    @SerialName("accepted")
    ACCEPTED,
    @SerialName("rejected")
    REJECTED,
    @SerialName("missed")
    MISSED,
    @SerialName("expired")
    EXPIRED
}

@Serializable
enum class RideClassification {
    @SerialName("good")
    GOOD,
    @SerialName("average")
    AVERAGE,
    @SerialName("bad")
    BAD,
    @SerialName("unknown")
    UNKNOWN
}

@Serializable
data class RideAnalytics(
    @SerialName("user_id")
    val userId: String,
    val date: String,
    @SerialName("total_rides_received")
    val totalRidesReceived: Int = 0,
    @SerialName("rides_accepted")
    val ridesAccepted: Int = 0,
    @SerialName("rides_rejected")
    val ridesRejected: Int = 0,
    @SerialName("rides_missed")
    val ridesMissed: Int = 0,
    @SerialName("total_earnings")
    val totalEarnings: Double = 0.0,
    @SerialName("total_distance_km")
    val totalDistanceKm: Double = 0.0,
    @SerialName("total_duration_mins")
    val totalDurationMins: Int = 0,
    @SerialName("avg_earnings_per_km")
    val avgEarningsPerKm: Double = 0.0,
    @SerialName("avg_earnings_per_hour")
    val avgEarningsPerHour: Double = 0.0,
    @SerialName("good_rides_count")
    val goodRidesCount: Int = 0,
    @SerialName("average_rides_count")
    val averageRidesCount: Int = 0,
    @SerialName("bad_rides_count")
    val badRidesCount: Int = 0
)

// For displaying ride info in overlay
data class RideDisplayInfo(
    val fareAmount: Double,
    val distanceKm: Double?,
    val earningsPerKm: Double?,
    val earningsPerHour: Double?,
    val riderRating: Double?,
    val classification: RideClassification,
    val sourceApp: String,
    val pickupLocation: String?,
    val dropoffLocation: String?
)

