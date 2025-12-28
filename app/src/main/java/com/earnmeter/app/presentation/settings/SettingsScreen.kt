package com.earnmeter.app.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.earnmeter.app.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToOverlaySettings: () -> Unit,
    onNavigateToTrackProfitsSettings: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Features Section
            SettingsSection(title = "Features") {
                SettingsItem(
                    icon = Icons.Outlined.TrendingUp,
                    title = state.trackProfitsName,
                    subtitle = "Configure ride classification ranges",
                    onClick = onNavigateToTrackProfitsSettings
                )
                
                SettingsItem(
                    icon = Icons.Outlined.Layers,
                    title = "Overlay Settings",
                    subtitle = "Customize popup appearance",
                    onClick = onNavigateToOverlaySettings
                )
            }
            
            // Notifications Section
            SettingsSection(title = "Notifications") {
                SettingsToggleItem(
                    icon = Icons.Outlined.Notifications,
                    title = "Ride Alerts",
                    subtitle = "Get notified about ride quality",
                    checked = state.rideAlertsEnabled,
                    onCheckedChange = { viewModel.toggleRideAlerts(it) }
                )
                
                SettingsToggleItem(
                    icon = Icons.Outlined.BarChart,
                    title = "Daily Summary",
                    subtitle = "Receive daily earnings summary",
                    checked = state.dailySummaryEnabled,
                    onCheckedChange = { viewModel.toggleDailySummary(it) }
                )
            }
            
            // Sync Section
            SettingsSection(title = "Data") {
                SettingsItem(
                    icon = Icons.Outlined.Sync,
                    title = "Sync Data",
                    subtitle = "Last synced: ${state.lastSyncTime}",
                    onClick = { viewModel.syncData() }
                )
                
                SettingsItem(
                    icon = Icons.Outlined.DeleteOutline,
                    title = "Clear Local Data",
                    subtitle = "Remove cached ride data",
                    onClick = { viewModel.showClearDataDialog() }
                )
            }
            
            // About Section
            SettingsSection(title = "About") {
                SettingsItem(
                    icon = Icons.Outlined.Info,
                    title = "App Version",
                    subtitle = BuildConfig.VERSION_NAME,
                    onClick = { }
                )
                
                SettingsItem(
                    icon = Icons.Outlined.Description,
                    title = "Terms of Service",
                    subtitle = null,
                    onClick = { viewModel.openTerms() }
                )
                
                SettingsItem(
                    icon = Icons.Outlined.PrivacyTip,
                    title = "Privacy Policy",
                    subtitle = null,
                    onClick = { viewModel.openPrivacy() }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
    
    // Clear data dialog
    if (state.showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideClearDataDialog() },
            title = { Text("Clear Local Data") },
            text = { Text("This will remove all cached ride data. Your data on the server will not be affected.") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.clearLocalData() }
                ) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideClearDataDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

