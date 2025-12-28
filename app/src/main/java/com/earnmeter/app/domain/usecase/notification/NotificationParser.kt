package com.earnmeter.app.domain.usecase.notification

import com.earnmeter.app.domain.model.RideDisplayInfo
import com.earnmeter.app.domain.model.RideClassification
import com.earnmeter.app.domain.model.SupportedApp
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Parses ride information from notification text.
 * Completely independent - can be unit tested without Android dependencies.
 */
@Singleton
class NotificationParser @Inject constructor() {

    companion object {
        // Regex patterns for extracting ride info
        private val FARE_PATTERN = Pattern.compile("₹\\s*([\\d,]+(?:\\.\\d{2})?)")
        private val DISTANCE_PATTERN = Pattern.compile("([\\d.]+)\\s*(?:km|KM|Km)")
        private val DURATION_PATTERN = Pattern.compile("([\\d]+)\\s*(?:min|mins|minutes|MIN)")
        private val RATING_PATTERN = Pattern.compile("([\\d.]+)\\s*(?:★|star|rating)", Pattern.CASE_INSENSITIVE)
        private val LOCATION_PATTERN = Pattern.compile("(?:from|pickup|pick up|→|to)\\s*([A-Za-z\\s]+)", Pattern.CASE_INSENSITIVE)
    }

    /**
     * Check if notification text matches patterns for a supported ride app
     */
    fun isRideNotification(text: String, supportedApp: SupportedApp): Boolean {
        return supportedApp.notificationPatterns.any { pattern ->
            text.contains(pattern, ignoreCase = true)
        }
    }

    /**
     * Parse ride details from notification text
     */
    fun parseNotification(text: String, appName: String): ParsedRideData {
        val fareAmount = extractFare(text)
        val distance = extractDistance(text)
        val duration = extractDuration(text)
        val rating = extractRating(text)
        
        val earningsPerKm = calculateEarningsPerKm(fareAmount, distance)
        val earningsPerHour = calculateEarningsPerHour(fareAmount, duration)
        
        return ParsedRideData(
            fareAmount = fareAmount,
            distanceKm = distance,
            durationMins = duration,
            riderRating = rating,
            earningsPerKm = earningsPerKm,
            earningsPerHour = earningsPerHour,
            sourceApp = appName,
            rawText = text
        )
    }

    private fun extractFare(text: String): Double {
        val matcher = FARE_PATTERN.matcher(text)
        return if (matcher.find()) {
            matcher.group(1)?.replace(",", "")?.toDoubleOrNull() ?: 0.0
        } else 0.0
    }

    private fun extractDistance(text: String): Double? {
        val matcher = DISTANCE_PATTERN.matcher(text)
        return if (matcher.find()) {
            matcher.group(1)?.toDoubleOrNull()
        } else null
    }

    private fun extractDuration(text: String): Int? {
        val matcher = DURATION_PATTERN.matcher(text)
        return if (matcher.find()) {
            matcher.group(1)?.toIntOrNull()
        } else null
    }

    private fun extractRating(text: String): Double? {
        val matcher = RATING_PATTERN.matcher(text)
        return if (matcher.find()) {
            matcher.group(1)?.toDoubleOrNull()
        } else null
    }

    private fun calculateEarningsPerKm(fare: Double, distance: Double?): Double? {
        return if (distance != null && distance > 0) fare / distance else null
    }

    private fun calculateEarningsPerHour(fare: Double, durationMins: Int?): Double? {
        return if (durationMins != null && durationMins > 0) {
            (fare / durationMins) * 60
        } else null
    }
}

/**
 * Data class for parsed ride information - pure data, no dependencies
 */
data class ParsedRideData(
    val fareAmount: Double,
    val distanceKm: Double?,
    val durationMins: Int?,
    val riderRating: Double?,
    val earningsPerKm: Double?,
    val earningsPerHour: Double?,
    val sourceApp: String,
    val rawText: String
)

