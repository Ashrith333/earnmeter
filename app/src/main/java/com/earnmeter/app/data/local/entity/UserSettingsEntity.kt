package com.earnmeter.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.earnmeter.app.domain.model.UserSettings

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey
    @ColumnInfo(name = "user_id")
    val userId: String,
    
    // Earnings per KM thresholds
    @ColumnInfo(name = "good_earnings_per_km")
    val goodEarningsPerKm: Double = 15.0,
    
    @ColumnInfo(name = "avg_earnings_per_km")
    val avgEarningsPerKm: Double = 10.0,
    
    @ColumnInfo(name = "bad_earnings_per_km")
    val badEarningsPerKm: Double = 5.0,
    
    // Earnings per hour thresholds
    @ColumnInfo(name = "good_earnings_per_hour")
    val goodEarningsPerHour: Double = 300.0,
    
    @ColumnInfo(name = "avg_earnings_per_hour")
    val avgEarningsPerHour: Double = 200.0,
    
    @ColumnInfo(name = "bad_earnings_per_hour")
    val badEarningsPerHour: Double = 100.0,
    
    // User rating thresholds
    @ColumnInfo(name = "good_rating")
    val goodRating: Double = 4.5,
    
    @ColumnInfo(name = "avg_rating")
    val avgRating: Double = 4.0,
    
    @ColumnInfo(name = "bad_rating")
    val badRating: Double = 3.5,
    
    // Overlay settings
    @ColumnInfo(name = "overlay_font_size")
    val overlayFontSize: Int = 14,
    
    @ColumnInfo(name = "overlay_position")
    val overlayPosition: String = "TOP_RIGHT",
    
    @ColumnInfo(name = "overlay_duration_ms")
    val overlayDurationMs: Long = 5000,
    
    @ColumnInfo(name = "overlay_opacity")
    val overlayOpacity: Float = 0.9f,
    
    // Feature toggles
    @ColumnInfo(name = "smart_assist_enabled")
    val smartAssistEnabled: Boolean = true,
    
    @ColumnInfo(name = "track_profits_enabled")
    val trackProfitsEnabled: Boolean = true,
    
    @ColumnInfo(name = "auto_suggest_ranges")
    val autoSuggestRanges: Boolean = false
) {
    fun toDomainModel(): UserSettings = UserSettings(
        userId = userId,
        goodEarningsPerKm = goodEarningsPerKm,
        avgEarningsPerKm = avgEarningsPerKm,
        badEarningsPerKm = badEarningsPerKm,
        goodEarningsPerHour = goodEarningsPerHour,
        avgEarningsPerHour = avgEarningsPerHour,
        badEarningsPerHour = badEarningsPerHour,
        goodRating = goodRating,
        avgRating = avgRating,
        badRating = badRating,
        overlayFontSize = overlayFontSize,
        overlayPosition = overlayPosition,
        overlayDurationMs = overlayDurationMs,
        overlayOpacity = overlayOpacity,
        smartAssistEnabled = smartAssistEnabled,
        trackProfitsEnabled = trackProfitsEnabled,
        autoSuggestRanges = autoSuggestRanges
    )
    
    companion object {
        fun fromDomainModel(settings: UserSettings): UserSettingsEntity = UserSettingsEntity(
            userId = settings.userId,
            goodEarningsPerKm = settings.goodEarningsPerKm,
            avgEarningsPerKm = settings.avgEarningsPerKm,
            badEarningsPerKm = settings.badEarningsPerKm,
            goodEarningsPerHour = settings.goodEarningsPerHour,
            avgEarningsPerHour = settings.avgEarningsPerHour,
            badEarningsPerHour = settings.badEarningsPerHour,
            goodRating = settings.goodRating,
            avgRating = settings.avgRating,
            badRating = settings.badRating,
            overlayFontSize = settings.overlayFontSize,
            overlayPosition = settings.overlayPosition,
            overlayDurationMs = settings.overlayDurationMs,
            overlayOpacity = settings.overlayOpacity,
            smartAssistEnabled = settings.smartAssistEnabled,
            trackProfitsEnabled = settings.trackProfitsEnabled,
            autoSuggestRanges = settings.autoSuggestRanges
        )
    }
}

