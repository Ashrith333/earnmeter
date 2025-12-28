package com.earnmeter.app.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    @SerialName("phone_number")
    val phoneNumber: String,
    @SerialName("full_name")
    val fullName: String? = null,
    val email: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null,
    @SerialName("is_active")
    val isActive: Boolean = true,
    @SerialName("profile_image_url")
    val profileImageUrl: String? = null,
    val city: String? = null,
    val state: String? = null
)

@Serializable
data class UserProfile(
    @SerialName("user_id")
    val userId: String,
    @SerialName("phone_number")
    val phoneNumber: String,
    @SerialName("full_name")
    val fullName: String? = null,
    val email: String? = null,
    val city: String? = null,
    val state: String? = null,
    @SerialName("profile_image_url")
    val profileImageUrl: String? = null
)

@Serializable
data class UserSettings(
    @SerialName("user_id")
    val userId: String,
    
    // Earnings per KM thresholds
    @SerialName("good_earnings_per_km")
    val goodEarningsPerKm: Double = 15.0,
    @SerialName("avg_earnings_per_km")
    val avgEarningsPerKm: Double = 10.0,
    @SerialName("bad_earnings_per_km")
    val badEarningsPerKm: Double = 5.0,
    
    // Earnings per hour thresholds
    @SerialName("good_earnings_per_hour")
    val goodEarningsPerHour: Double = 300.0,
    @SerialName("avg_earnings_per_hour")
    val avgEarningsPerHour: Double = 200.0,
    @SerialName("bad_earnings_per_hour")
    val badEarningsPerHour: Double = 100.0,
    
    // User rating thresholds
    @SerialName("good_rating")
    val goodRating: Double = 4.5,
    @SerialName("avg_rating")
    val avgRating: Double = 4.0,
    @SerialName("bad_rating")
    val badRating: Double = 3.5,
    
    // Overlay settings
    @SerialName("overlay_font_size")
    val overlayFontSize: Int = 14,
    @SerialName("overlay_position")
    val overlayPosition: String = "TOP_RIGHT", // TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, CENTER
    @SerialName("overlay_duration_ms")
    val overlayDurationMs: Long = 5000,
    @SerialName("overlay_opacity")
    val overlayOpacity: Float = 0.9f,
    
    // Feature toggles
    @SerialName("smart_assist_enabled")
    val smartAssistEnabled: Boolean = true,
    @SerialName("track_profits_enabled")
    val trackProfitsEnabled: Boolean = true,
    @SerialName("auto_suggest_ranges")
    val autoSuggestRanges: Boolean = false
)

