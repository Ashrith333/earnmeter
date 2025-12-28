package com.earnmeter.app.presentation.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.earnmeter.app.domain.model.Ride
import com.earnmeter.app.domain.model.RideAction
import com.earnmeter.app.domain.model.RideClassification
import com.earnmeter.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToRideHistory: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()
    
    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            viewModel.refresh()
            pullToRefreshState.endRefresh()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Earn Meter",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings"
                        )
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = "Profile"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .nestedScroll(pullToRefreshState.nestedScrollConnection)
        ) {
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Today's Stats Card
                    item {
                        TodayStatsCard(
                            earnings = state.todayStats.earnings,
                            rideCount = state.todayStats.rideCount,
                            distance = state.todayStats.distance,
                            acceptedCount = state.todayStats.acceptedCount,
                            rejectedCount = state.todayStats.rejectedCount
                        )
                    }
                    
                    // Feature Toggles
                    item {
                        FeatureTogglesSection(
                            smartAssistName = state.smartAssistName,
                            trackProfitsName = state.trackProfitsName,
                            smartAssistEnabled = state.smartAssistEnabled,
                            trackProfitsEnabled = state.trackProfitsEnabled,
                            onSmartAssistToggle = { viewModel.toggleSmartAssist(it) },
                            onTrackProfitsToggle = { viewModel.toggleTrackProfits(it) }
                        )
                    }
                    
                    // Quick Actions
                    item {
                        QuickActionsSection(
                            onNavigateToRideHistory = onNavigateToRideHistory,
                            onNavigateToSettings = onNavigateToSettings
                        )
                    }
                    
                    // Recent Rides Header
                    if (state.recentRides.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Recent Rides",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                TextButton(onClick = onNavigateToRideHistory) {
                                    Text("See All")
                                }
                            }
                        }
                        
                        // Recent Rides
                        items(state.recentRides.take(5)) { ride ->
                            RideCard(ride = ride)
                        }
                    }
                }
            }
            
            PullToRefreshContainer(
                state = pullToRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
fun TodayStatsCard(
    earnings: Double,
    rideCount: Int,
    distance: Double,
    acceptedCount: Int,
    rejectedCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            EmeraldGreen.copy(alpha = 0.8f),
                            DarkEmerald.copy(alpha = 0.9f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Text(
                text = "Today's Earnings",
                style = MaterialTheme.typography.titleMedium,
                color = WarmWhite.copy(alpha = 0.8f)
            )
            
            Text(
                text = "₹${String.format("%,.0f", earnings)}",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = WarmWhite
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    icon = Icons.Outlined.DirectionsCar,
                    label = "Rides",
                    value = "$rideCount"
                )
                StatItem(
                    icon = Icons.Outlined.Route,
                    label = "Distance",
                    value = "${String.format("%.1f", distance)} km"
                )
                StatItem(
                    icon = Icons.Outlined.CheckCircle,
                    label = "Accepted",
                    value = "$acceptedCount"
                )
                StatItem(
                    icon = Icons.Outlined.Cancel,
                    label = "Rejected",
                    value = "$rejectedCount"
                )
            }
        }
    }
}

@Composable
fun StatItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = WarmWhite.copy(alpha = 0.8f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = WarmWhite
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = WarmWhite.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun FeatureTogglesSection(
    smartAssistName: String,
    trackProfitsName: String,
    smartAssistEnabled: Boolean,
    trackProfitsEnabled: Boolean,
    onSmartAssistToggle: (Boolean) -> Unit,
    onTrackProfitsToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            FeatureToggleRow(
                icon = Icons.Filled.Assistant,
                name = smartAssistName,
                description = "Analyze and classify ride notifications",
                enabled = smartAssistEnabled,
                onToggle = onSmartAssistToggle
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            
            FeatureToggleRow(
                icon = Icons.Filled.TrendingUp,
                name = trackProfitsName,
                description = "Track earnings and ride quality",
                enabled = trackProfitsEnabled,
                onToggle = onTrackProfitsToggle
            )
        }
    }
}

@Composable
fun FeatureToggleRow(
    icon: ImageVector,
    name: String,
    description: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    if (enabled) 
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    else 
                        MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Switch(
            checked = enabled,
            onCheckedChange = onToggle
        )
    }
}

@Composable
fun QuickActionsSection(
    onNavigateToRideHistory: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Outlined.History,
            label = "Ride History",
            onClick = onNavigateToRideHistory
        )
        QuickActionCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Outlined.Tune,
            label = "Settings",
            onClick = onNavigateToSettings
        )
    }
}

@Composable
fun QuickActionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
fun RideCard(ride: Ride) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Classification indicator
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        when (ride.classification) {
                            RideClassification.GOOD -> ClassificationColors.good.copy(alpha = 0.15f)
                            RideClassification.AVERAGE -> ClassificationColors.average.copy(alpha = 0.15f)
                            RideClassification.BAD -> ClassificationColors.bad.copy(alpha = 0.15f)
                            RideClassification.UNKNOWN -> ClassificationColors.unknown.copy(alpha = 0.15f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (ride.action) {
                        RideAction.ACCEPTED -> Icons.Default.Check
                        RideAction.REJECTED -> Icons.Default.Close
                        else -> Icons.Default.DirectionsCar
                    },
                    contentDescription = null,
                    tint = when (ride.classification) {
                        RideClassification.GOOD -> ClassificationColors.good
                        RideClassification.AVERAGE -> ClassificationColors.average
                        RideClassification.BAD -> ClassificationColors.bad
                        RideClassification.UNKNOWN -> ClassificationColors.unknown
                    }
                )
            }
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = ride.sourceApp,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ride.distanceKm?.let {
                        Text(
                            text = "${String.format("%.1f", it)} km",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    ride.earningsPerKm?.let {
                        Text(
                            text = "₹${String.format("%.1f", it)}/km",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "₹${String.format("%.0f", ride.fareAmount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = ride.action.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    color = when (ride.action) {
                        RideAction.ACCEPTED -> ClassificationColors.good
                        RideAction.REJECTED -> ClassificationColors.bad
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

