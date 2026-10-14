package com.example.ottracker.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,                      // Near-white (active icons, selected items)
    onPrimary = DarkBackground,                 // Near-black on white
    primaryContainer = DarkSurfaceElevated,     // Charcoal container
    onPrimaryContainer = DarkTextPrimary,       // Near-white text
    secondary = DarkSecondary,                  // Gray (inactive)
    onSecondary = DarkTextPrimary,
    secondaryContainer = DarkSurface,
    onSecondaryContainer = DarkTextSecondary,
    tertiary = GoldAccentDark,                  // Gold — only for ₱ values
    onTertiary = Color.Black,
    background = DarkBackground,               // Near-black
    onBackground = DarkTextPrimary,            // Near-white
    surface = DarkSurface,                     // Dark gray cards
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceElevated,      // Charcoal
    onSurfaceVariant = DarkTextSecondary,      // Mid-gray
    outline = DarkSurfaceBorder                // Dark borders
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,                     // Deep black (active icons, selected items)
    onPrimary = Color.White,                    // White on black
    primaryContainer = LightSurfaceElevated,    // Off-white container
    onPrimaryContainer = LightTextPrimary,      // Near-black text
    secondary = LightSecondary,                 // Gray (inactive)
    onSecondary = Color.White,
    secondaryContainer = LightSurfaceElevated,
    onSecondaryContainer = LightTextSecondary,
    tertiary = GoldAccent,                      // Gold — only for ₱ values
    onTertiary = Color.Black,
    background = LightBackground,              // Pure white
    onBackground = LightTextPrimary,           // Near-black
    surface = LightSurface,                    // White cards
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceElevated,     // Off-white
    onSurfaceVariant = LightTextSecondary,     // Mid-gray
    outline = LightSurfaceBorder               // Light gray borders
)

@Composable
fun OTTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
