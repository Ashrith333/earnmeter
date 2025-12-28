package com.earnmeter.app.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.earnmeter.app.presentation.theme.EmeraldGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverlaySettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: OverlaySettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Overlay Settings") },
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
            // Preview Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Preview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Sample overlay preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = when (state.position) {
                            "TOP_LEFT" -> Alignment.TopStart
                            "TOP_RIGHT" -> Alignment.TopEnd
                            "BOTTOM_LEFT" -> Alignment.BottomStart
                            "BOTTOM_RIGHT" -> Alignment.BottomEnd
                            else -> Alignment.Center
                        }
                    ) {
                        OverlayPreview(
                            fontSize = state.fontSize,
                            opacity = state.opacity
                        )
                    }
                }
            }
            
            // Font Size
            SettingSlider(
                title = "Font Size",
                value = state.fontSize.toFloat(),
                valueRange = 10f..24f,
                steps = 13,
                valueLabel = "${state.fontSize} sp",
                onValueChange = { viewModel.updateFontSize(it.toInt()) }
            )
            
            // Display Duration
            SettingSlider(
                title = "Display Duration",
                value = state.durationMs.toFloat() / 1000f,
                valueRange = 2f..15f,
                steps = 12,
                valueLabel = "${state.durationMs / 1000} seconds",
                onValueChange = { viewModel.updateDuration((it * 1000).toLong()) }
            )
            
            // Opacity
            SettingSlider(
                title = "Opacity",
                value = state.opacity,
                valueRange = 0.5f..1f,
                valueLabel = "${(state.opacity * 100).toInt()}%",
                onValueChange = { viewModel.updateOpacity(it) }
            )
            
            // Position
            PositionSelector(
                selectedPosition = state.position,
                onPositionSelected = { viewModel.updatePosition(it) }
            )
            
            // Success message
            if (state.saved) {
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
        }
    }
}

@Composable
fun OverlayPreview(
    fontSize: Int,
    opacity: Float
) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(EmeraldGreen.copy(alpha = opacity))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "✓ GOOD RIDE",
            fontSize = (fontSize + 2).sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary
        )
        Text(
            text = "₹250",
            fontSize = (fontSize + 6).sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary
        )
        Text(
            text = "8.5 km • ₹29/km",
            fontSize = fontSize.sp,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun SettingSlider(
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    valueLabel: String,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = valueLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps
        )
    }
}

@Composable
fun PositionSelector(
    selectedPosition: String,
    onPositionSelected: (String) -> Unit
) {
    Column {
        Text(
            text = "Position",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Position grid
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PositionButton(
                    modifier = Modifier.weight(1f),
                    label = "Top Left",
                    position = "TOP_LEFT",
                    isSelected = selectedPosition == "TOP_LEFT",
                    onClick = { onPositionSelected("TOP_LEFT") }
                )
                PositionButton(
                    modifier = Modifier.weight(1f),
                    label = "Top Right",
                    position = "TOP_RIGHT",
                    isSelected = selectedPosition == "TOP_RIGHT",
                    onClick = { onPositionSelected("TOP_RIGHT") }
                )
            }
            
            PositionButton(
                modifier = Modifier.fillMaxWidth(),
                label = "Center",
                position = "CENTER",
                isSelected = selectedPosition == "CENTER",
                onClick = { onPositionSelected("CENTER") }
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PositionButton(
                    modifier = Modifier.weight(1f),
                    label = "Bottom Left",
                    position = "BOTTOM_LEFT",
                    isSelected = selectedPosition == "BOTTOM_LEFT",
                    onClick = { onPositionSelected("BOTTOM_LEFT") }
                )
                PositionButton(
                    modifier = Modifier.weight(1f),
                    label = "Bottom Right",
                    position = "BOTTOM_RIGHT",
                    isSelected = selectedPosition == "BOTTOM_RIGHT",
                    onClick = { onPositionSelected("BOTTOM_RIGHT") }
                )
            }
        }
    }
}

@Composable
fun PositionButton(
    modifier: Modifier = Modifier,
    label: String,
    position: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                else
                    MaterialTheme.colorScheme.surfaceVariant
            )
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

