package com.earnmeter.app.receiver

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import com.earnmeter.app.service.NotificationListenerService
import com.earnmeter.app.service.OverlayService

class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            
            // Ask Android to rebind notification listener after reboot
            android.service.notification.NotificationListenerService.requestRebind(
                ComponentName(context, NotificationListenerService::class.java)
            )

            // Start the overlay service on boot
            val serviceIntent = Intent(context, OverlayService::class.java).apply {
                action = OverlayService.ACTION_START_FOREGROUND
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }
}

