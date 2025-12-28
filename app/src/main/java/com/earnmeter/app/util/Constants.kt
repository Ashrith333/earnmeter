package com.earnmeter.app.util

/**
 * Application constants
 */
object Constants {
    // Supabase Configuration
    const val SUPABASE_URL = "https://jfdcguuegggerpvvgxdc.supabase.co"
    
    // Feature name keys (for remote config)
    const val CONFIG_SMART_ASSIST_NAME = "feature_smart_assist_name"
    const val CONFIG_TRACK_PROFITS_NAME = "feature_track_profits_name"
    
    // Notification patterns refresh interval
    const val PATTERNS_REFRESH_INTERVAL_HOURS = 24L
    
    // Overlay defaults
    const val DEFAULT_OVERLAY_DURATION_MS = 5000L
    const val DEFAULT_OVERLAY_FONT_SIZE = 14
    const val DEFAULT_OVERLAY_OPACITY = 0.9f
    const val DEFAULT_OVERLAY_POSITION = "TOP_RIGHT"
    
    // Pagination
    const val DEFAULT_PAGE_SIZE = 20
    
    // Sync
    const val SYNC_WORK_NAME = "ride_sync_work"
    const val SYNC_INTERVAL_HOURS = 1L
}

/**
 * Supported overlay positions
 */
enum class OverlayPosition(val displayName: String) {
    TOP_LEFT("Top Left"),
    TOP_RIGHT("Top Right"),
    CENTER("Center"),
    BOTTOM_LEFT("Bottom Left"),
    BOTTOM_RIGHT("Bottom Right")
}

