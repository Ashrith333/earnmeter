package com.earnmeter.app.presentation

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.earnmeter.app.presentation.navigation.EarnMeterNavHost
import com.earnmeter.app.service.OverlayService
import com.earnmeter.app.presentation.theme.EarnMeterTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        if (Settings.canDrawOverlays(this)) {
            val serviceIntent = Intent(this, OverlayService::class.java).apply {
                action = OverlayService.ACTION_START_FOREGROUND
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        }

        setContent {
            EarnMeterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EarnMeterNavHost()
                }
            }
        }
    }
}

