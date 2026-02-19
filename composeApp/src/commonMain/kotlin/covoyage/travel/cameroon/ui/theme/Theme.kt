package covoyage.travel.cameroon.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = CameroonGreen,
    onPrimary = White,
    primaryContainer = CameroonGreenLight.copy(alpha = 0.15f),
    onPrimaryContainer = CameroonGreenDark,
    secondary = CameroonYellow,
    onSecondary = Charcoal,
    secondaryContainer = CameroonYellowLight.copy(alpha = 0.3f),
    onSecondaryContainer = Charcoal,
    tertiary = CameroonRed,
    onTertiary = White,
    error = ErrorRed,
    onError = White,
    background = OffWhite,
    onBackground = Charcoal,
    surface = White,
    onSurface = Charcoal,
    surfaceVariant = LightGray,
    onSurfaceVariant = DarkGray,
    outline = MediumGray,
)

private val DarkColorScheme = darkColorScheme(
    primary = CameroonGreenLight,
    onPrimary = CameroonGreenDark,
    primaryContainer = CameroonGreen.copy(alpha = 0.3f),
    onPrimaryContainer = CameroonGreenLight,
    secondary = CameroonYellow,
    onSecondary = Charcoal,
    secondaryContainer = CameroonYellow.copy(alpha = 0.2f),
    onSecondaryContainer = CameroonYellowLight,
    tertiary = CameroonRedLight,
    onTertiary = Charcoal,
    error = CameroonRedLight,
    onError = Charcoal,
    background = DarkBackground,
    onBackground = OffWhite,
    surface = DarkSurface,
    onSurface = OffWhite,
    surfaceVariant = DarkCard,
    onSurfaceVariant = LightGray,
    outline = DarkGray,
)

@Composable
fun CoVoyageTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CoVoyageTypography,
        content = content
    )
}
