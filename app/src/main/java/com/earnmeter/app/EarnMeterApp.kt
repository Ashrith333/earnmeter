package com.earnmeter.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class EarnMeterApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val notificationManager = getSystemService(NotificationManager::class.java)

        // Foreground Service Channel
        val serviceChannel = NotificationChannel(
            CHANNEL_FOREGROUND_SERVICE,
            "Background Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Keeps the app running in the background to monitor ride notifications"
            setShowBadge(false)
        }

        // Ride Alerts Channel
        val alertsChannel = NotificationChannel(
            CHANNEL_RIDE_ALERTS,
            "Ride Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for new ride opportunities"
            enableVibration(true)
            setShowBadge(true)
        }

        // Analytics Channel
        val analyticsChannel = NotificationChannel(
            CHANNEL_ANALYTICS,
            "Daily Analytics",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Daily earnings summary and analytics"
        }

        notificationManager.createNotificationChannels(
            listOf(serviceChannel, alertsChannel, analyticsChannel)
        )
    }

    companion object {
        const val CHANNEL_FOREGROUND_SERVICE = "foreground_service_channel"
        const val CHANNEL_RIDE_ALERTS = "ride_alerts_channel"
        const val CHANNEL_ANALYTICS = "analytics_channel"
    }
}

