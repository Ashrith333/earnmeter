package com.earnmeter.app.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Vibrant color palette inspired by earnings/money theme
val EmeraldGreen = Color(0xFF00C853)
val DarkEmerald = Color(0xFF00A844)
val DeepCharcoal = Color(0xFF121212)
val RichBlack = Color(0xFF0A0A0A)
val WarmWhite = Color(0xFFFAFAFA)
val CoolGray = Color(0xFF9E9E9E)
val AccentGold = Color(0xFFFFD700)
val GoodGreen = Color(0xFF2E7D32)
val AverageOrange = Color(0xFFF57C00)
val BadRed = Color(0xFFC62828)
val SoftMint = Color(0xFFE8F5E9)
val DarkSurface = Color(0xFF1E1E1E)

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldGreen,
    onPrimary = DeepCharcoal,
    primaryContainer = DarkEmerald,
    onPrimaryContainer = WarmWhite,
    secondary = AccentGold,
    onSecondary = DeepCharcoal,
    secondaryContainer = Color(0xFF33691E),
    onSecondaryContainer = SoftMint,
    tertiary = Color(0xFF00BFA5),
    onTertiary = DeepCharcoal,
    background = RichBlack,
    onBackground = WarmWhite,
    surface = DarkSurface,
    onSurface = WarmWhite,
    surfaceVariant = Color(0xFF2D2D2D),
    onSurfaceVariant = CoolGray,
    error = BadRed,
    onError = WarmWhite,
    outline = Color(0xFF3D3D3D)
)

private val LightColorScheme = lightColorScheme(
    primary = DarkEmerald,
    onPrimary = WarmWhite,
    primaryContainer = SoftMint,
    onPrimaryContainer = Color(0xFF002200),
    secondary = AccentGold,
    onSecondary = DeepCharcoal,
    secondaryContainer = Color(0xFFFFF8E1),
    onSecondaryContainer = Color(0xFF332800),
    tertiary = Color(0xFF00897B),
    onTertiary = WarmWhite,
    background = WarmWhite,
    onBackground = DeepCharcoal,
    surface = Color(0xFFFFFFFF),
    onSurface = DeepCharcoal,
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF616161),
    error = BadRed,
    onError = WarmWhite,
    outline = Color(0xFFE0E0E0)
)

@Composable
fun EarnMeterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = EarnMeterTypography,
        content = content
    )
}

// Classification colors helper
object ClassificationColors {
    val good = GoodGreen
    val average = AverageOrange
    val bad = BadRed
    val unknown = CoolGray
}

