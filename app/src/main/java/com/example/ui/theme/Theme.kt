package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val WakeQuestDarkColorScheme = darkColorScheme(
    primary = SunriseOrangeBright,
    onPrimary = DarkBackground,
    primaryContainer = SunriseOrange,
    onPrimaryContainer = TextPrimary,
    secondary = SunriseAmber,
    onSecondary = DarkBackground,
    secondaryContainer = DarkSurfaceElevated,
    onSecondaryContainer = SunriseGold,
    tertiary = SunriseCoral,
    onTertiary = TextPrimary,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = DarkSurfaceBorder
)

// For consistency with the energetic sunrise request, both themes share the signature dark sunrise styling
private val WakeQuestLightColorScheme = WakeQuestDarkColorScheme

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Preserve signature sunrise branding
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = WakeQuestDarkColorScheme,
        typography = Typography,
        content = content
    )
}
