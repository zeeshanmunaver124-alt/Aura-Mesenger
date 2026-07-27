package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = VibeVioletPrimary,
    onPrimary = Color.White,
    primaryContainer = VibeVioletLight.copy(alpha = 0.2f),
    onPrimaryContainer = VibeVioletLight,
    secondary = VibeCyanSecondary,
    onSecondary = Color.Black,
    tertiary = VibeCoralAccent,
    background = VibeDarkBackground,
    onBackground = TextPrimaryDark,
    surface = VibeDarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = VibeDarkCard,
    onSurfaceVariant = TextSecondaryDark,
    outline = VibeDarkCardHighlight
)

private val LightColorScheme = lightColorScheme(
    primary = VibeVioletPrimary,
    onPrimary = Color.White,
    primaryContainer = VibeVioletLight,
    onPrimaryContainer = VibeVioletDarkContainer,
    secondary = VibeCyanSecondary,
    onSecondary = Color.White,
    tertiary = VibeCoralAccent,
    background = VibeLightBackground,
    onBackground = TextPrimaryLight,
    surface = VibeLightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = VibeLightCard,
    onSurfaceVariant = TextSecondaryLight,
    outline = Color(0xFFE7E0EC)
)

@Composable
fun VibeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Keep legacy wrapper for backwards compatibility with tests
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    VibeTheme(darkTheme = darkTheme, content = content)
}

