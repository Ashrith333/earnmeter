package com.earnmeter.app.presentation.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.earnmeter.app.BuildConfig
import com.earnmeter.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackProfitsSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: TrackProfitsSettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(BuildConfig.FEATURE_TRACK_PROFITS_NAME) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.saveSettings() },
                        enabled = !state.isLoading
                    ) {
                        Text("Save")
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Set ranges to classify rides as Good, Average, or Bad based on earnings and rider ratings.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            // Suggest Ranges Button (V2 Feature)
            OutlinedButton(
                onClick = { viewModel.suggestRanges() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = !state.isLoading
            ) {
                Icon(Icons.Default.AutoAwesome, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Suggest Best Ranges for My City")
            }
            
            // Earnings per KM Section
            RangeSection(
                title = "Earnings per KM (₹)",
                goodValue = state.goodEarningsPerKm,
                avgValue = state.avgEarningsPerKm,
                badValue = state.badEarningsPerKm,
                valueRange = 1f..50f,
                onGoodChange = { viewModel.updateGoodEarningsPerKm(it) },
                onAvgChange = { viewModel.updateAvgEarningsPerKm(it) },
                onBadChange = { viewModel.updateBadEarningsPerKm(it) }
            )
            
            // Earnings per Hour Section
            RangeSection(
                title = "Earnings per Hour (₹)",
                goodValue = state.goodEarningsPerHour,
                avgValue = state.avgEarningsPerHour,
                badValue = state.badEarningsPerHour,
                valueRange = 50f..500f,
                onGoodChange = { viewModel.updateGoodEarningsPerHour(it) },
                onAvgChange = { viewModel.updateAvgEarningsPerHour(it) },
                onBadChange = { viewModel.updateBadEarningsPerHour(it) }
            )
            
            // User Rating Section
            RatingSection(
                goodValue = state.goodRating,
                avgValue = state.avgRating,
                badValue = state.badRating,
                onGoodChange = { viewModel.updateGoodRating(it) },
                onAvgChange = { viewModel.updateAvgRating(it) },
                onBadChange = { viewModel.updateBadRating(it) }
            )
            
            // Success message
            AnimatedVisibility(visible = state.saved) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Settings saved successfully",
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            // Error message
            AnimatedVisibility(visible = state.error != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = state.error ?: "",
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun RangeSection(
    title: String,
    goodValue: Double,
    avgValue: Double,
    badValue: Double,
    valueRange: ClosedFloatingPointRange<Float>,
    onGoodChange: (Double) -> Unit,
    onAvgChange: (Double) -> Unit,
    onBadChange: (Double) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            // Visual range bar
            RangeBar(
                goodValue = goodValue.toFloat(),
                avgValue = avgValue.toFloat(),
                badValue = badValue.toFloat(),
                valueRange = valueRange
            )
            
            // Good threshold
            RangeSlider(
                label = "Good",
                color = ClassificationColors.good,
                value = goodValue.toFloat(),
                valueRange = valueRange,
                onValueChange = { onGoodChange(it.toDouble()) }
            )
            
            // Average threshold
            RangeSlider(
                label = "Average",
                color = ClassificationColors.average,
                value = avgValue.toFloat(),
                valueRange = valueRange,
                onValueChange = { onAvgChange(it.toDouble()) }
            )
            
            // Bad threshold
            RangeSlider(
                label = "Bad",
                color = ClassificationColors.bad,
                value = badValue.toFloat(),
                valueRange = valueRange,
                onValueChange = { onBadChange(it.toDouble()) }
            )
        }
    }
}

@Composable
fun RangeBar(
    goodValue: Float,
    avgValue: Float,
    badValue: Float,
    valueRange: ClosedFloatingPointRange<Float>
) {
    val totalRange = valueRange.endInclusive - valueRange.start
    val badWidth = ((badValue - valueRange.start) / totalRange).coerceIn(0f, 1f)
    val avgWidth = ((avgValue - valueRange.start) / totalRange).coerceIn(0f, 1f)
    val goodWidth = ((goodValue - valueRange.start) / totalRange).coerceIn(0f, 1f)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        Box(
            modifier = Modifier
                .weight(badWidth.coerceAtLeast(0.01f))
                .fillMaxHeight()
                .background(ClassificationColors.bad)
        )
        Box(
            modifier = Modifier
                .weight((avgWidth - badWidth).coerceAtLeast(0.01f))
                .fillMaxHeight()
                .background(ClassificationColors.average)
        )
        Box(
            modifier = Modifier
                .weight((1f - avgWidth).coerceAtLeast(0.01f))
                .fillMaxHeight()
                .background(ClassificationColors.good)
        )
    }
    
    // Labels
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "₹${valueRange.start.toInt()}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "₹${valueRange.endInclusive.toInt()}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun RangeSlider(
    label: String,
    color: androidx.compose.ui.graphics.Color,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(60.dp)
        )
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color.copy(alpha = 0.7f)
            )
        )
        
        Text(
            text = "₹${String.format("%.1f", value)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(60.dp)
        )
    }
}

@Composable
fun RatingSection(
    goodValue: Double,
    avgValue: Double,
    badValue: Double,
    onGoodChange: (Double) -> Unit,
    onAvgChange: (Double) -> Unit,
    onBadChange: (Double) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "User Rating Threshold",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            // Good rating
            RatingSlider(
                label = "Good",
                color = ClassificationColors.good,
                value = goodValue.toFloat(),
                onValueChange = { onGoodChange(it.toDouble()) }
            )
            
            // Average rating
            RatingSlider(
                label = "Average",
                color = ClassificationColors.average,
                value = avgValue.toFloat(),
                onValueChange = { onAvgChange(it.toDouble()) }
            )
            
            // Bad rating
            RatingSlider(
                label = "Bad",
                color = ClassificationColors.bad,
                value = badValue.toFloat(),
                onValueChange = { onBadChange(it.toDouble()) }
            )
        }
    }
}

@Composable
fun RatingSlider(
    label: String,
    color: androidx.compose.ui.graphics.Color,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(60.dp)
        )
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 1f..5f,
            steps = 39, // 0.1 increments
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color.copy(alpha = 0.7f)
            )
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.width(60.dp)
        ) {
            Text(
                text = "★ ${String.format("%.1f", value)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

