package com.theeasiestway.stereoar.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Created by Alexey Loboda on 29.01.2022
 */

private val LightPalette = lightColorScheme(
    primary = Colors.primary_purple,
    surface = Colors.surface_default,
    background = Colors.surface_background,
    onSurface = Colors.on_surface_primary,
)

private val DarkPalette = darkColorScheme(
    primary = Colors.primary_purple,
    surface = Colors.on_surface_primary,
    background = Colors.on_surface_secondary,
    onSurface = Colors.surface_default,
)

@Composable
fun AppTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (useDarkTheme) DarkPalette else LightPalette,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}