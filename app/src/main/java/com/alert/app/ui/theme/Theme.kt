package com.alert.app.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Brand Colors
val SafeGreen = Color(0xFF2ECC71)
val SafeGreenDark = Color(0xFF1A7A43)
val WarningOrange = Color(0xFFFF9500)
val WarningOrangeDark = Color(0xFFCC7700)
val DangerRed = Color(0xFFFF3B30)
val DangerRedDark = Color(0xFFCC2020)

// Dark theme background palette
val BackgroundDark = Color(0xFF0A0E1A)
val SurfaceDark = Color(0xFF121829)
val SurfaceVariantDark = Color(0xFF1C2333)
val CardDark = Color(0xFF161D2E)
val OnBackgroundDark = Color(0xFFE8EAED)
val OnSurfaceDark = Color(0xFFCDD0D9)
val OutlineDark = Color(0xFF3A4560)
val PrimaryBlue = Color(0xFF4A90E2)
val PrimaryBlueDark = Color(0xFF2C6EC7)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1A3A6B),
    onPrimaryContainer = Color(0xFFB8D4FF),
    secondary = SafeGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF0D3D22),
    onSecondaryContainer = Color(0xFF9FEAD0),
    tertiary = WarningOrange,
    onTertiary = Color.White,
    error = DangerRed,
    onError = Color.White,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = Color(0xFFA0AABF),
    outline = OutlineDark
)

@Composable
fun AlertTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography(),
        content = content
    )
}
