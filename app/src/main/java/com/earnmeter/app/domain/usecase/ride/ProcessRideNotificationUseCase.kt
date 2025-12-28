package com.earnmeter.app.domain.usecase.ride

import com.earnmeter.app.data.repository.RideRepository
import com.earnmeter.app.data.repository.SettingsRepository
import com.earnmeter.app.domain.model.Ride
import com.earnmeter.app.domain.model.RideDisplayInfo
import com.earnmeter.app.domain.model.SupportedApp
import com.earnmeter.app.domain.model.UserSettings
import com.earnmeter.app.domain.usecase.classification.RideClassifier
import com.earnmeter.app.domain.usecase.notification.NotificationParser
import com.earnmeter.app.domain.usecase.notification.ParsedRideData
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case that orchestrates the full ride notification processing flow.
 * Coordinates between parser, classifier, and repository.
 * Each component can be tested independently.
 */
@Singleton
class ProcessRideNotificationUseCase @Inject constructor(
    private val notificationParser: NotificationParser,
    private val rideClassifier: RideClassifier,
    private val rideRepository: RideRepository,
    private val settingsRepository: SettingsRepository
) {

    /**
     * Process a notification and return display info for overlay
     */
    suspend fun execute(
        userId: String,
        notificationText: String,
        supportedApp: SupportedApp,
        userSettings: UserSettings
    ): Result<ProcessedRideResult> {
        return try {
            // Step 1: Parse the notification
            val parsedData = notificationParser.parseNotification(
                text = notificationText,
                appName = supportedApp.appName
            )
            
            // Step 2: Classify the ride
            val classification = rideClassifier.classify(
                earningsPerKm = parsedData.earningsPerKm,
                earningsPerHour = parsedData.earningsPerHour,
                riderRating = parsedData.riderRating,
                settings = userSettings
            )
            
            // Step 3: Create ride entity
            val ride = Ride(
                userId = userId,
                sourceApp = supportedApp.appName,
                fareAmount = parsedData.fareAmount,
                distanceKm = parsedData.distanceKm,
                estimatedDurationMins = parsedData.durationMins,
                earningsPerKm = parsedData.earningsPerKm,
                earningsPerHour = parsedData.earningsPerHour,
                riderRating = parsedData.riderRating,
                classification = classification,
                notificationReceivedAt = Instant.now().toString(),
                rawNotificationData = parsedData.rawText
            )
            
            // Step 4: Save to repository
            val savedRide = rideRepository.insertRide(ride)
            
            // Step 5: Create display info
            val displayInfo = RideDisplayInfo(
                fareAmount = parsedData.fareAmount,
                distanceKm = parsedData.distanceKm,
                earningsPerKm = parsedData.earningsPerKm,
                earningsPerHour = parsedData.earningsPerHour,
                riderRating = parsedData.riderRating,
                classification = classification,
                sourceApp = supportedApp.appName,
                pickupLocation = null,
                dropoffLocation = null
            )
            
            Result.success(ProcessedRideResult(
                ride = savedRide.getOrNull() ?: ride,
                displayInfo = displayInfo,
                parsedData = parsedData
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class ProcessedRideResult(
    val ride: Ride,
    val displayInfo: RideDisplayInfo,
    val parsedData: ParsedRideData
)

