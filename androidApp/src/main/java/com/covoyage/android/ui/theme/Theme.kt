package com.covoyage.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Primary Brand Colors - Futuristic Blue/Purple gradient
val PrimaryLight = Color(0xFF6366F1) // Indigo
val PrimaryDark = Color(0xFF4F46E5)
val SecondaryLight = Color(0xFF8B5CF6) // Purple
val SecondaryDark = Color(0xFF7C3AED)
val TertiaryLight = Color(0xFF06B6D4) // Cyan
val TertiaryDark = Color(0xFF0891B2)

// Accent Colors
val AccentGreen = Color(0xFF10B981)
val AccentOrange = Color(0xFFF59E0B)
val AccentRed = Color(0xFFEF4444)

// Background Colors
val BackgroundLight = Color(0xFFFAFAFA)
val BackgroundDark = Color(0xFF0F172A)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceDark = Color(0xFF1E293B)

// Light Color Scheme
private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEEF2FF),
    onPrimaryContainer = PrimaryDark,
    
    secondary = SecondaryLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF5F3FF),
    onSecondaryContainer = SecondaryDark,
    
    tertiary = TertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFCFFAFE),
    onTertiaryContainer = TertiaryDark,
    
    background = BackgroundLight,
    onBackground = Color(0xFF1F2937),
    surface = SurfaceLight,
    onSurface = Color(0xFF1F2937),
    
    error = AccentRed,
    onError = Color.White
)

// Dark Color Scheme
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryLight,
    onPrimary = Color.White,
    primaryContainer = PrimaryDark,
    onPrimaryContainer = Color(0xFFE0E7FF),
    
    secondary = SecondaryLight,
    onSecondary = Color.White,
    secondaryContainer = SecondaryDark,
    onSecondaryContainer = Color(0xFFEDE9FE),
    
    tertiary = TertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = TertiaryDark,
    onTertiaryContainer = Color(0xFFCFFAFE),
    
    background = BackgroundDark,
    onBackground = Color(0xFFF8FAFC),
    surface = SurfaceDark,
    onSurface = Color(0xFFF8FAFC),
    
    error = AccentRed,
    onError = Color.White
)

@Composable
fun CoVoyageTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
