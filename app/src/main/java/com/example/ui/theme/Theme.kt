package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SimuDarkColorScheme = darkColorScheme(
    primary = SimuCyan,
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004F58),
    onPrimaryContainer = Color(0xFF97F0FF),
    secondary = SimuViolet,
    onSecondary = Color(0xFF24005A),
    secondaryContainer = Color(0xFF3B1879),
    onSecondaryContainer = Color(0xFFEADBFF),
    tertiary = SimuEmerald,
    background = SimuDeepSpace,
    onBackground = SimuTextPrimary,
    surface = SimuSurfaceDark,
    onSurface = SimuTextPrimary,
    surfaceVariant = SimuSurfaceElevated,
    onSurfaceVariant = SimuTextSecondary,
    outline = SimuCardBorder,
    outlineVariant = Color(0xFF1A2640),
    error = SimuCrimson
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SimuDarkColorScheme,
        typography = Typography,
        content = content
    )
}
