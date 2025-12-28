package com.earnmeter.app.service

import android.app.Notification
import android.content.Intent
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.earnmeter.app.data.repository.AuthRepository
import com.earnmeter.app.data.repository.SettingsRepository
import com.earnmeter.app.domain.model.DefaultSupportedApps
import com.earnmeter.app.domain.model.SupportedApp
import com.earnmeter.app.domain.model.UserSettings
import com.earnmeter.app.domain.usecase.notification.NotificationParser
import com.earnmeter.app.domain.usecase.overlay.OverlayManager
import com.earnmeter.app.domain.usecase.ride.ProcessRideNotificationUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Notification Listener Service for monitoring ride-sharing app notifications.
 * 
 * ARCHITECTURE NOTES:
 * - This service is ONLY responsible for:
 *   1. Receiving notifications from Android system
 *   2. Filtering relevant notifications
 *   3. Delegating processing to use cases
 * 
 * - All business logic is in separate, testable components:
 *   - NotificationParser: Extracts ride data from text
 *   - RideClassifier: Classifies rides as good/bad
 *   - ProcessRideNotificationUseCase: Orchestrates the flow
 *   - OverlayManager: Handles overlay display
 */
@AndroidEntryPoint
class RideNotificationListenerService : NotificationListenerService() {

    companion object {
        private const val TAG = "RideNotificationListener"
    }

    // =====================================================
    // DEPENDENCIES - Each handles a specific responsibility
    // =====================================================
    
    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var notificationParser: NotificationParser

    @Inject
    lateinit var processRideUseCase: ProcessRideNotificationUseCase

    @Inject
    lateinit var overlayManager: OverlayManager

    // =====================================================
    // STATE - Cached data for quick access
    // =====================================================
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var supportedApps: List<SupportedApp> = DefaultSupportedApps.allApps
    private var userSettings: UserSettings? = null
    private var userId: String? = null
    private var isInitialized = false

    // =====================================================
    // LIFECYCLE
    // =====================================================

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        initialize()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.d(TAG, "Service destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return super.onBind(intent)
    }

    // =====================================================
    // INITIALIZATION - Load required data
    // =====================================================

    private fun initialize() {
        serviceScope.launch {
            try {
                // Load supported apps (can be updated remotely)
                loadSupportedApps()
                
                // Get current user
                userId = authRepository.currentUserId
                
                // Load user settings
                loadUserSettings()
                
                isInitialized = true
                Log.d(TAG, "Service initialized successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize service", e)
            }
        }
    }

    private suspend fun loadSupportedApps() {
        val result = settingsRepository.getSupportedApps()
        if (result.isSuccess) {
            supportedApps = result.getOrNull()?.takeIf { it.isNotEmpty() } 
                ?: DefaultSupportedApps.allApps
        }
    }

    private suspend fun loadUserSettings() {
        userId?.let { uid ->
            val result = settingsRepository.getSettings(uid)
            if (result.isSuccess) {
                userSettings = result.getOrNull()
            }
        }
    }

    // =====================================================
    // NOTIFICATION HANDLING - Receive and filter
    // =====================================================

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // Skip if not initialized
        if (!isInitialized) {
            Log.d(TAG, "Service not initialized yet, skipping notification")
            return
        }

        // Find matching supported app
        val supportedApp = findMatchingApp(sbn.packageName) ?: return

        // Check if smart assist is enabled
        if (userSettings?.smartAssistEnabled != true) {
            Log.d(TAG, "Smart Assist disabled, skipping notification")
            return
        }

        // Extract notification text
        val notificationText = extractNotificationText(sbn.notification)
        if (notificationText.isBlank()) return

        // Check if this is a ride notification
        if (!notificationParser.isRideNotification(notificationText, supportedApp)) {
            return
        }

        Log.d(TAG, "Ride notification from ${supportedApp.appName}: ${notificationText.take(100)}...")

        // Process the notification
        processNotification(notificationText, supportedApp)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // Could be used to track if user interacted with notification
    }

    // =====================================================
    // PROCESSING - Delegate to use cases
    // =====================================================

    private fun processNotification(text: String, supportedApp: SupportedApp) {
        val currentUserId = userId ?: return
        val currentSettings = userSettings ?: return

        serviceScope.launch {
            try {
                // Delegate to use case for full processing
                val result = processRideUseCase.execute(
                    userId = currentUserId,
                    notificationText = text,
                    supportedApp = supportedApp,
                    userSettings = currentSettings
                )

                if (result.isSuccess) {
                    val processedResult = result.getOrNull()!!
                    
                    // Show overlay with ride info
                    overlayManager.showRideOverlay(
                        rideInfo = processedResult.displayInfo,
                        settings = currentSettings
                    )
                    
                    Log.d(TAG, "Processed ride: ₹${processedResult.ride.fareAmount} - ${processedResult.ride.classification}")
                } else {
                    Log.e(TAG, "Failed to process ride", result.exceptionOrNull())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing notification", e)
            }
        }
    }

    // =====================================================
    // HELPER METHODS
    // =====================================================

    private fun findMatchingApp(packageName: String): SupportedApp? {
        return supportedApps.find { it.packageName == packageName }
    }

    private fun extractNotificationText(notification: Notification): String {
        val extras = notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""
        return "$title $text $bigText"
    }

    // =====================================================
    // PUBLIC API - For refreshing settings
    // =====================================================

    /**
     * Call this when user updates settings to refresh cached values
     */
    fun refreshSettings() {
        serviceScope.launch {
            loadUserSettings()
            loadSupportedApps()
        }
    }
}
