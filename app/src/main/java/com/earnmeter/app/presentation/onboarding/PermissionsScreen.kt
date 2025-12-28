package com.earnmeter.app.presentation.onboarding

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import com.earnmeter.app.service.OverlayService
import com.earnmeter.app.presentation.theme.EmeraldGreen

data class PermissionItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val isGranted: Boolean,
    val onRequest: () -> Unit
)

@Composable
fun PermissionsScreen(
    onPermissionsGranted: () -> Unit
) {
    val context = LocalContext.current
    
    var notificationListenerGranted by remember { 
        mutableStateOf(isNotificationListenerEnabled(context)) 
    }
    var overlayPermissionGranted by remember { 
        mutableStateOf(Settings.canDrawOverlays(context)) 
    }
    var batteryOptimizationDisabled by remember { 
        mutableStateOf(isBatteryOptimizationDisabled(context)) 
    }
    var notificationPermissionGranted by remember {
        mutableStateOf(areNotificationsEnabled(context))
    }
    
    // Check permissions periodically
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(500)
            notificationListenerGranted = isNotificationListenerEnabled(context)
            overlayPermissionGranted = Settings.canDrawOverlays(context)
            batteryOptimizationDisabled = isBatteryOptimizationDisabled(context)
            notificationPermissionGranted = areNotificationsEnabled(context)
        }
    }
    
    val allPermissionsGranted = notificationListenerGranted && 
            overlayPermissionGranted && 
            batteryOptimizationDisabled
    
    val permissions = listOf(
        PermissionItem(
            title = "Notification Access",
            description = "Read ride notifications from Uber, Ola, Rapido to analyze offers",
            icon = Icons.Default.Notifications,
            isGranted = notificationListenerGranted,
            onRequest = {
                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                context.startActivity(intent)
            }
        ),
        PermissionItem(
            title = "Display Over Apps",
            description = "Show ride analysis popup on top of other apps",
            icon = Icons.Default.Layers,
            isGranted = overlayPermissionGranted,
            onRequest = {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                )
                context.startActivity(intent)
            }
        ),
        PermissionItem(
            title = "Battery Optimization",
            description = "Allow app to run in background without restrictions",
            icon = Icons.Default.BatteryChargingFull,
            isGranted = batteryOptimizationDisabled,
            onRequest = {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                context.startActivity(intent)
            }
        )
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        // Header icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Required Permissions",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "Earn Meter needs these permissions to monitor and analyze ride offers for you",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )
        
        // Permission cards
        permissions.forEachIndexed { index, permission ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically(
                    initialOffsetY = { 50 * (index + 1) }
                )
            ) {
                PermissionCard(
                    permission = permission,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Continue button
        Button(
            onClick = {
                if (allPermissionsGranted) {
                    // Start overlay service
                    val serviceIntent = Intent(context, OverlayService::class.java).apply {
                        action = OverlayService.ACTION_START_FOREGROUND
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                    onPermissionsGranted()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = allPermissionsGranted,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = if (allPermissionsGranted) "Continue" else "Grant All Permissions",
                style = MaterialTheme.typography.titleMedium
            )
        }
        
        if (!allPermissionsGranted) {
            Text(
                text = "Please grant all permissions to continue",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun PermissionCard(
    permission: PermissionItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (permission.isGranted) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (permission.isGranted) 
                            EmeraldGreen.copy(alpha = 0.2f)
                        else 
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (permission.isGranted) 
                        Icons.Default.Check 
                    else 
                        permission.icon,
                    contentDescription = null,
                    tint = if (permission.isGranted) 
                        EmeraldGreen 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = permission.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = permission.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (!permission.isGranted) {
                FilledTonalButton(
                    onClick = permission.onRequest,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Grant")
                }
            }
        }
    }
}

private fun isNotificationListenerEnabled(context: Context): Boolean {
    val packageName = context.packageName
    val flat = Settings.Secure.getString(
        context.contentResolver,
        "enabled_notification_listeners"
    )
    return flat?.contains(packageName) == true
}

private fun isBatteryOptimizationDisabled(context: Context): Boolean {
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    return powerManager.isIgnoringBatteryOptimizations(context.packageName)
}

private fun areNotificationsEnabled(context: Context): Boolean {
    return NotificationManagerCompat.from(context).areNotificationsEnabled()
}

