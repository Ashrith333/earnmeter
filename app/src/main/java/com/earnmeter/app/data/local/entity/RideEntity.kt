package com.earnmeter.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.earnmeter.app.domain.model.Ride
import com.earnmeter.app.domain.model.RideAction
import com.earnmeter.app.domain.model.RideClassification

@Entity(tableName = "rides")
data class RideEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "user_id")
    val userId: String,
    
    @ColumnInfo(name = "source_app")
    val sourceApp: String,
    
    @ColumnInfo(name = "pickup_location")
    val pickupLocation: String? = null,
    
    @ColumnInfo(name = "dropoff_location")
    val dropoffLocation: String? = null,
    
    @ColumnInfo(name = "distance_km")
    val distanceKm: Double? = null,
    
    @ColumnInfo(name = "estimated_duration_mins")
    val estimatedDurationMins: Int? = null,
    
    @ColumnInfo(name = "fare_amount")
    val fareAmount: Double,
    
    @ColumnInfo(name = "surge_multiplier")
    val surgeMultiplier: Double? = null,
    
    @ColumnInfo(name = "tip_amount")
    val tipAmount: Double? = null,
    
    @ColumnInfo(name = "earnings_per_km")
    val earningsPerKm: Double? = null,
    
    @ColumnInfo(name = "earnings_per_hour")
    val earningsPerHour: Double? = null,
    
    @ColumnInfo(name = "rider_rating")
    val riderRating: Double? = null,
    
    val action: RideAction = RideAction.PENDING,
    
    @ColumnInfo(name = "action_timestamp")
    val actionTimestamp: String? = null,
    
    val classification: RideClassification = RideClassification.UNKNOWN,
    
    @ColumnInfo(name = "notification_received_at")
    val notificationReceivedAt: String,
    
    @ColumnInfo(name = "created_at")
    val createdAt: String? = null,
    
    @ColumnInfo(name = "raw_notification_data")
    val rawNotificationData: String? = null,
    
    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false
) {
    fun toDomainModel(): Ride = Ride(
        id = id,
        userId = userId,
        sourceApp = sourceApp,
        pickupLocation = pickupLocation,
        dropoffLocation = dropoffLocation,
        distanceKm = distanceKm,
        estimatedDurationMins = estimatedDurationMins,
        fareAmount = fareAmount,
        surgeMultiplier = surgeMultiplier,
        tipAmount = tipAmount,
        earningsPerKm = earningsPerKm,
        earningsPerHour = earningsPerHour,
        riderRating = riderRating,
        action = action,
        actionTimestamp = actionTimestamp,
        classification = classification,
        notificationReceivedAt = notificationReceivedAt,
        createdAt = createdAt,
        rawNotificationData = rawNotificationData
    )
    
    companion object {
        fun fromDomainModel(ride: Ride, isSynced: Boolean = false): RideEntity = RideEntity(
            id = ride.id ?: java.util.UUID.randomUUID().toString(),
            userId = ride.userId,
            sourceApp = ride.sourceApp,
            pickupLocation = ride.pickupLocation,
            dropoffLocation = ride.dropoffLocation,
            distanceKm = ride.distanceKm,
            estimatedDurationMins = ride.estimatedDurationMins,
            fareAmount = ride.fareAmount,
            surgeMultiplier = ride.surgeMultiplier,
            tipAmount = ride.tipAmount,
            earningsPerKm = ride.earningsPerKm,
            earningsPerHour = ride.earningsPerHour,
            riderRating = ride.riderRating,
            action = ride.action,
            actionTimestamp = ride.actionTimestamp,
            classification = ride.classification,
            notificationReceivedAt = ride.notificationReceivedAt,
            createdAt = ride.createdAt,
            rawNotificationData = ride.rawNotificationData,
            isSynced = isSynced
        )
    }
}

