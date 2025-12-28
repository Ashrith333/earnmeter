package com.earnmeter.app.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Admin-configured suggested ranges based on location/city.
 * This is for V2 feature where app can suggest optimal ranges.
 */
@Serializable
data class AdminSuggestedRanges(
    val id: String? = null,
    val city: String,
    val state: String? = null,
    val country: String = "India",
    
    // Suggested earnings per KM
    @SerialName("suggested_good_per_km")
    val suggestedGoodPerKm: Double,
    @SerialName("suggested_avg_per_km")
    val suggestedAvgPerKm: Double,
    @SerialName("suggested_bad_per_km")
    val suggestedBadPerKm: Double,
    
    // Suggested earnings per hour
    @SerialName("suggested_good_per_hour")
    val suggestedGoodPerHour: Double,
    @SerialName("suggested_avg_per_hour")
    val suggestedAvgPerHour: Double,
    @SerialName("suggested_bad_per_hour")
    val suggestedBadPerHour: Double,
    
    // Suggested rating thresholds
    @SerialName("suggested_good_rating")
    val suggestedGoodRating: Double,
    @SerialName("suggested_avg_rating")
    val suggestedAvgRating: Double,
    @SerialName("suggested_bad_rating")
    val suggestedBadRating: Double,
    
    @SerialName("is_active")
    val isActive: Boolean = true,
    @SerialName("updated_at")
    val updatedAt: String? = null,
    @SerialName("updated_by")
    val updatedBy: String? = null
)

/**
 * App configuration that can be updated remotely
 */
@Serializable
data class AppConfig(
    val id: String? = null,
    @SerialName("config_key")
    val configKey: String,
    @SerialName("config_value")
    val configValue: String,
    val description: String? = null,
    @SerialName("is_active")
    val isActive: Boolean = true,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

/**
 * Supported ride-sharing app package names for notification filtering
 */
@Serializable
data class SupportedApp(
    val id: String? = null,
    @SerialName("package_name")
    val packageName: String,
    @SerialName("app_name")
    val appName: String,
    @SerialName("notification_patterns")
    val notificationPatterns: List<String>,
    @SerialName("is_active")
    val isActive: Boolean = true
)

// Default supported apps
object DefaultSupportedApps {
    val UBER = SupportedApp(
        packageName = "com.ubercab.driver",
        appName = "Uber Driver",
        notificationPatterns = listOf(
            "New trip request",
            "Trip nearby",
            "₹"
        )
    )
    
    val OLA = SupportedApp(
        packageName = "com.olacabs.oladriver",
        appName = "Ola Driver",
        notificationPatterns = listOf(
            "New ride",
            "Booking",
            "₹"
        )
    )
    
    val RAPIDO = SupportedApp(
        packageName = "com.rapido.rider",
        appName = "Rapido Captain",
        notificationPatterns = listOf(
            "New order",
            "Trip request",
            "₹"
        )
    )
    
    val allApps = listOf(UBER, OLA, RAPIDO)
}

