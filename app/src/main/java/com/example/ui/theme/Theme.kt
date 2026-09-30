package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = WidbyPrimary,
    onPrimary = Color.Black,
    secondary = WidbySecondary,
    onSecondary = Color.Black,
    tertiary = WidbyTertiary,
    onTertiary = Color.Black,
    background = WidbyBackground,
    onBackground = WidbyTextPrimary,
    surface = WidbySurface,
    onSurface = WidbyTextPrimary,
    surfaceVariant = WidbySurfaceElevated,
    onSurfaceVariant = WidbyTextSecondary,
    outline = WidbySurfaceBorder,
    error = WidbyError,
    onError = Color.White
)

private val LightColorScheme = DarkColorScheme // Default dark theme by design

@Composable
fun WidbyTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    WidbyTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}

