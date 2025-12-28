package com.earnmeter.app.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.earnmeter.app.EarnMeterApp
import com.earnmeter.app.R
import com.earnmeter.app.domain.model.RideClassification
import com.earnmeter.app.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private val handler = Handler(Looper.getMainLooper())
    private var hideRunnable: Runnable? = null

    companion object {
        const val ACTION_SHOW_RIDE = "com.earnmeter.app.ACTION_SHOW_RIDE"
        const val ACTION_HIDE = "com.earnmeter.app.ACTION_HIDE"
        const val ACTION_START_FOREGROUND = "com.earnmeter.app.ACTION_START_FOREGROUND"
        const val ACTION_STOP_FOREGROUND = "com.earnmeter.app.ACTION_STOP_FOREGROUND"
        
        const val EXTRA_FARE_AMOUNT = "fare_amount"
        const val EXTRA_DISTANCE = "distance"
        const val EXTRA_EARNINGS_PER_KM = "earnings_per_km"
        const val EXTRA_EARNINGS_PER_HOUR = "earnings_per_hour"
        const val EXTRA_RATING = "rating"
        const val EXTRA_CLASSIFICATION = "classification"
        const val EXTRA_SOURCE_APP = "source_app"
        const val EXTRA_DURATION_MS = "duration_ms"
        const val EXTRA_FONT_SIZE = "font_size"
        const val EXTRA_POSITION = "position"
        const val EXTRA_OPACITY = "opacity"
        
        private const val NOTIFICATION_ID = 1001
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_FOREGROUND -> {
                startForegroundService()
            }
            ACTION_STOP_FOREGROUND -> {
                removeOverlay()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            ACTION_SHOW_RIDE -> {
                if (canDrawOverlays()) {
                    val fareAmount = intent.getDoubleExtra(EXTRA_FARE_AMOUNT, 0.0)
                    val distance = intent.getDoubleExtra(EXTRA_DISTANCE, 0.0)
                    val earningsPerKm = intent.getDoubleExtra(EXTRA_EARNINGS_PER_KM, 0.0)
                    val earningsPerHour = intent.getDoubleExtra(EXTRA_EARNINGS_PER_HOUR, 0.0)
                    val rating = intent.getDoubleExtra(EXTRA_RATING, 0.0)
                    val classification = intent.getStringExtra(EXTRA_CLASSIFICATION) ?: "UNKNOWN"
                    val sourceApp = intent.getStringExtra(EXTRA_SOURCE_APP) ?: ""
                    val durationMs = intent.getLongExtra(EXTRA_DURATION_MS, 5000L)
                    val fontSize = intent.getIntExtra(EXTRA_FONT_SIZE, 14)
                    val position = intent.getStringExtra(EXTRA_POSITION) ?: "TOP_RIGHT"
                    val opacity = intent.getFloatExtra(EXTRA_OPACITY, 0.9f)
                    
                    showRideOverlay(
                        fareAmount = fareAmount,
                        distance = distance,
                        earningsPerKm = earningsPerKm,
                        earningsPerHour = earningsPerHour,
                        rating = rating,
                        classification = RideClassification.valueOf(classification),
                        sourceApp = sourceApp,
                        durationMs = durationMs,
                        fontSize = fontSize,
                        position = position,
                        opacity = opacity
                    )
                }
            }
            ACTION_HIDE -> {
                removeOverlay()
            }
        }
        
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        removeOverlay()
    }

    private fun startForegroundService() {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, EarnMeterApp.CHANNEL_FOREGROUND_SERVICE)
            .setContentTitle("Earn Meter")
            .setContentText("Monitoring ride notifications")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun canDrawOverlays(): Boolean {
        return Settings.canDrawOverlays(this)
    }

    private fun showRideOverlay(
        fareAmount: Double,
        distance: Double,
        earningsPerKm: Double,
        earningsPerHour: Double,
        rating: Double,
        classification: RideClassification,
        sourceApp: String,
        durationMs: Long,
        fontSize: Int,
        position: String,
        opacity: Float
    ) {
        handler.post {
            // Remove existing overlay first
            removeOverlay()
            
            // Create overlay view
            overlayView = createOverlayView(
                fareAmount = fareAmount,
                distance = distance,
                earningsPerKm = earningsPerKm,
                earningsPerHour = earningsPerHour,
                rating = rating,
                classification = classification,
                sourceApp = sourceApp,
                fontSize = fontSize,
                opacity = opacity
            )
            
            // Create layout params
            val layoutParams = createLayoutParams(position, opacity)
            
            // Add view to window
            try {
                windowManager.addView(overlayView, layoutParams)
                
                // Schedule removal
                hideRunnable = Runnable { removeOverlay() }
                handler.postDelayed(hideRunnable!!, durationMs)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun createOverlayView(
        fareAmount: Double,
        distance: Double,
        earningsPerKm: Double,
        earningsPerHour: Double,
        rating: Double,
        classification: RideClassification,
        sourceApp: String,
        fontSize: Int,
        opacity: Float
    ): View {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 16, 24, 16)
            
            // Set background color based on classification
            val bgColor = when (classification) {
                RideClassification.GOOD -> 0xCC2E7D32.toInt() // Green with alpha
                RideClassification.AVERAGE -> 0xCCF57C00.toInt() // Orange with alpha
                RideClassification.BAD -> 0xCCC62828.toInt() // Red with alpha
                RideClassification.UNKNOWN -> 0xCC424242.toInt() // Gray with alpha
            }
            setBackgroundColor(bgColor)
            
            alpha = opacity
        }
        
        // Classification label
        val classificationLabel = TextView(this).apply {
            text = when (classification) {
                RideClassification.GOOD -> "✓ GOOD RIDE"
                RideClassification.AVERAGE -> "~ AVERAGE RIDE"
                RideClassification.BAD -> "✗ BAD RIDE"
                RideClassification.UNKNOWN -> "? UNKNOWN"
            }
            setTextColor(0xFFFFFFFF.toInt())
            textSize = (fontSize + 4).toFloat()
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 8)
        }
        container.addView(classificationLabel)
        
        // Fare amount
        val fareView = TextView(this).apply {
            text = "₹${String.format("%.0f", fareAmount)}"
            setTextColor(0xFFFFFFFF.toInt())
            textSize = (fontSize + 8).toFloat()
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 8)
        }
        container.addView(fareView)
        
        // Details container
        val detailsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        
        if (distance > 0) {
            val distanceView = TextView(this).apply {
                text = "${String.format("%.1f", distance)} km"
                setTextColor(0xFFFFFFFF.toInt())
                textSize = fontSize.toFloat()
                setPadding(8, 0, 8, 0)
            }
            detailsContainer.addView(distanceView)
        }
        
        if (earningsPerKm > 0) {
            val epkView = TextView(this).apply {
                text = "₹${String.format("%.1f", earningsPerKm)}/km"
                setTextColor(0xFFFFFFFF.toInt())
                textSize = fontSize.toFloat()
                setPadding(8, 0, 8, 0)
            }
            detailsContainer.addView(epkView)
        }
        
        if (earningsPerHour > 0) {
            val ephView = TextView(this).apply {
                text = "₹${String.format("%.0f", earningsPerHour)}/hr"
                setTextColor(0xFFFFFFFF.toInt())
                textSize = fontSize.toFloat()
                setPadding(8, 0, 8, 0)
            }
            detailsContainer.addView(ephView)
        }
        
        container.addView(detailsContainer)
        
        // Rating if available
        if (rating > 0) {
            val ratingView = TextView(this).apply {
                text = "★ ${String.format("%.1f", rating)}"
                setTextColor(0xFFFFFFFF.toInt())
                textSize = fontSize.toFloat()
                gravity = Gravity.CENTER
                setPadding(0, 8, 0, 0)
            }
            container.addView(ratingView)
        }
        
        // Source app
        val sourceView = TextView(this).apply {
            text = sourceApp
            setTextColor(0xAAFFFFFF.toInt())
            textSize = (fontSize - 2).toFloat()
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 0)
        }
        container.addView(sourceView)
        
        // Make view draggable
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        
        container.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val params = v.layoutParams as WindowManager.LayoutParams
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val params = v.layoutParams as WindowManager.LayoutParams
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(v, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    // If it was a tap (not a drag), dismiss
                    if (Math.abs(event.rawX - initialTouchX) < 10 && 
                        Math.abs(event.rawY - initialTouchY) < 10) {
                        removeOverlay()
                    }
                    true
                }
                else -> false
            }
        }
        
        return container
    }

    private fun createLayoutParams(position: String, opacity: Float): WindowManager.LayoutParams {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
        
        val gravity = when (position) {
            "TOP_LEFT" -> Gravity.TOP or Gravity.START
            "TOP_RIGHT" -> Gravity.TOP or Gravity.END
            "BOTTOM_LEFT" -> Gravity.BOTTOM or Gravity.START
            "BOTTOM_RIGHT" -> Gravity.BOTTOM or Gravity.END
            "CENTER" -> Gravity.CENTER
            else -> Gravity.TOP or Gravity.END
        }
        
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            this.gravity = gravity
            x = 16
            y = 100
        }
    }

    private fun removeOverlay() {
        handler.post {
            hideRunnable?.let { handler.removeCallbacks(it) }
            hideRunnable = null
            
            overlayView?.let { view ->
                try {
                    windowManager.removeView(view)
                } catch (e: Exception) {
                    // View might already be removed
                }
            }
            overlayView = null
        }
    }
}

