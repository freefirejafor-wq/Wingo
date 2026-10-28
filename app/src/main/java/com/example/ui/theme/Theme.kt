package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CyberDarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = Color(0xFF001F24),
    primaryContainer = CyberCardBg,
    onPrimaryContainer = CyberCyan,
    secondary = NeonGreen,
    onSecondary = Color(0xFF00210B),
    secondaryContainer = Color(0xFF06291A),
    onSecondaryContainer = NeonGreen,
    tertiary = NeonViolet,
    onTertiary = Color(0xFF1F003B),
    background = CyberObsidian,
    onBackground = TextPrimary,
    surface = CyberSurface,
    onSurface = TextPrimary,
    surfaceVariant = CyberCardBg,
    onSurfaceVariant = TextSecondary,
    error = NeonRed,
    onError = Color.White,
    outline = CyberBorder.copy(alpha = 0.35f)
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CyberDarkColorScheme,
        typography = Typography,
        content = content
    )
}
