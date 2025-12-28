package com.earnmeter.app.domain.usecase.overlay

import android.content.Context
import android.content.Intent
import android.os.Build
import com.earnmeter.app.domain.model.RideDisplayInfo
import com.earnmeter.app.domain.model.UserSettings
import com.earnmeter.app.service.OverlayService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages overlay display operations.
 * Abstracts the overlay service communication.
 */
@Singleton
class OverlayManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Show ride information overlay
     */
    fun showRideOverlay(
        rideInfo: RideDisplayInfo,
        settings: UserSettings
    ) {
        val intent = Intent(context, OverlayService::class.java).apply {
            action = OverlayService.ACTION_SHOW_RIDE
            putExtra(OverlayService.EXTRA_FARE_AMOUNT, rideInfo.fareAmount)
            putExtra(OverlayService.EXTRA_DISTANCE, rideInfo.distanceKm ?: 0.0)
            putExtra(OverlayService.EXTRA_EARNINGS_PER_KM, rideInfo.earningsPerKm ?: 0.0)
            putExtra(OverlayService.EXTRA_EARNINGS_PER_HOUR, rideInfo.earningsPerHour ?: 0.0)
            putExtra(OverlayService.EXTRA_RATING, rideInfo.riderRating ?: 0.0)
            putExtra(OverlayService.EXTRA_CLASSIFICATION, rideInfo.classification.name)
            putExtra(OverlayService.EXTRA_SOURCE_APP, rideInfo.sourceApp)
            putExtra(OverlayService.EXTRA_DURATION_MS, settings.overlayDurationMs)
            putExtra(OverlayService.EXTRA_FONT_SIZE, settings.overlayFontSize)
            putExtra(OverlayService.EXTRA_POSITION, settings.overlayPosition)
            putExtra(OverlayService.EXTRA_OPACITY, settings.overlayOpacity)
        }
        
        context.startService(intent)
    }

    /**
     * Hide any visible overlay
     */
    fun hideOverlay() {
        val intent = Intent(context, OverlayService::class.java).apply {
            action = OverlayService.ACTION_HIDE
        }
        context.startService(intent)
    }

    /**
     * Start the foreground overlay service
     */
    fun startForegroundService() {
        val intent = Intent(context, OverlayService::class.java).apply {
            action = OverlayService.ACTION_START_FOREGROUND
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    /**
     * Stop the overlay service
     */
    fun stopService() {
        val intent = Intent(context, OverlayService::class.java).apply {
            action = OverlayService.ACTION_STOP_FOREGROUND
        }
        context.startService(intent)
    }
}

