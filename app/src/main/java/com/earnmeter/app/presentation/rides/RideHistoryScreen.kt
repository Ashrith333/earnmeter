package com.earnmeter.app.presentation.rides

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.earnmeter.app.domain.model.Ride
import com.earnmeter.app.domain.model.RideAction
import com.earnmeter.app.domain.model.RideClassification
import com.earnmeter.app.presentation.theme.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideHistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: RideHistoryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()
    var selectedFilter by remember { mutableStateOf<RideAction?>(null) }
    
    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            viewModel.refresh()
            pullToRefreshState.endRefresh()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ride History") },
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
        ) {
            // Filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { 
                        selectedFilter = null
                        viewModel.filterByAction(null)
                    },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = selectedFilter == RideAction.ACCEPTED,
                    onClick = { 
                        selectedFilter = RideAction.ACCEPTED
                        viewModel.filterByAction(RideAction.ACCEPTED)
                    },
                    label = { Text("Accepted") },
                    leadingIcon = if (selectedFilter == RideAction.ACCEPTED) {
                        { Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp)) }
                    } else null
                )
                FilterChip(
                    selected = selectedFilter == RideAction.REJECTED,
                    onClick = { 
                        selectedFilter = RideAction.REJECTED
                        viewModel.filterByAction(RideAction.REJECTED)
                    },
                    label = { Text("Rejected") },
                    leadingIcon = if (selectedFilter == RideAction.REJECTED) {
                        { Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp)) }
                    } else null
                )
            }
            
            // Summary stats
            AnimatedVisibility(visible = state.rides.isNotEmpty()) {
                SummaryCard(
                    totalRides = state.rides.size,
                    totalEarnings = state.totalEarnings,
                    totalDistance = state.totalDistance,
                    avgEarningsPerKm = state.avgEarningsPerKm
                )
            }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(pullToRefreshState.nestedScrollConnection)
            ) {
                if (state.isLoading && state.rides.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (state.rides.isEmpty()) {
                    EmptyState()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.rides) { ride ->
                            RideDetailCard(ride = ride)
                        }
                        
                        // Load more indicator
                        if (state.hasMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                
                                LaunchedEffect(Unit) {
                                    viewModel.loadMore()
                                }
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
}

@Composable
fun SummaryCard(
    totalRides: Int,
    totalEarnings: Double,
    totalDistance: Double,
    avgEarningsPerKm: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SummaryItem(
                label = "Rides",
                value = "$totalRides"
            )
            SummaryItem(
                label = "Earnings",
                value = "₹${String.format("%,.0f", totalEarnings)}"
            )
            SummaryItem(
                label = "Distance",
                value = "${String.format("%.1f", totalDistance)} km"
            )
            SummaryItem(
                label = "Avg ₹/km",
                value = "₹${String.format("%.1f", avgEarningsPerKm)}"
            )
        }
    }
}

@Composable
fun SummaryItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun RideDetailCard(ride: Ride) {
    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("MMM d, h:mm a")
            .withZone(ZoneId.systemDefault())
    }
    
    val formattedDate = try {
        val instant = Instant.parse(ride.notificationReceivedAt)
        dateFormatter.format(instant)
    } catch (e: Exception) {
        ride.notificationReceivedAt
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Classification indicator
                    Box(
                        modifier = Modifier
                            .size(40.dp)
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
                            imageVector = when (ride.classification) {
                                RideClassification.GOOD -> Icons.Default.ThumbUp
                                RideClassification.AVERAGE -> Icons.Default.ThumbsUpDown
                                RideClassification.BAD -> Icons.Default.ThumbDown
                                RideClassification.UNKNOWN -> Icons.Default.QuestionMark
                            },
                            contentDescription = null,
                            tint = when (ride.classification) {
                                RideClassification.GOOD -> ClassificationColors.good
                                RideClassification.AVERAGE -> ClassificationColors.average
                                RideClassification.BAD -> ClassificationColors.bad
                                RideClassification.UNKNOWN -> ClassificationColors.unknown
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Text(
                            text = ride.sourceApp,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Status chip
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            text = ride.action.name.lowercase()
                                .replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = when (ride.action) {
                                RideAction.ACCEPTED -> Icons.Default.Check
                                RideAction.REJECTED -> Icons.Default.Close
                                RideAction.MISSED -> Icons.Default.Schedule
                                RideAction.EXPIRED -> Icons.Default.Timer
                                else -> Icons.Default.Pending
                            },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = when (ride.action) {
                            RideAction.ACCEPTED -> ClassificationColors.good.copy(alpha = 0.15f)
                            RideAction.REJECTED -> ClassificationColors.bad.copy(alpha = 0.15f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            HorizontalDivider()
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Details row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DetailItem(
                    label = "Fare",
                    value = "₹${String.format("%.0f", ride.fareAmount)}",
                    isHighlighted = true
                )
                
                ride.distanceKm?.let {
                    DetailItem(
                        label = "Distance",
                        value = "${String.format("%.1f", it)} km"
                    )
                }
                
                ride.earningsPerKm?.let {
                    DetailItem(
                        label = "₹/km",
                        value = "₹${String.format("%.1f", it)}"
                    )
                }
                
                ride.earningsPerHour?.let {
                    DetailItem(
                        label = "₹/hr",
                        value = "₹${String.format("%.0f", it)}"
                    )
                }
                
                ride.riderRating?.let {
                    DetailItem(
                        label = "Rating",
                        value = "★ ${String.format("%.1f", it)}"
                    )
                }
            }
        }
    }
}

@Composable
fun DetailItem(
    label: String,
    value: String,
    isHighlighted: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium,
            color = if (isHighlighted) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.DirectionsCar,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No rides yet",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = "Your ride notifications will appear here once you start receiving them.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

