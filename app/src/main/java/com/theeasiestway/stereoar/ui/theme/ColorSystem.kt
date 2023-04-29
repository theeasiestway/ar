package com.theeasiestway.stereoar.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class ColorSystem(
    val accent: Color,
    val primary: Color,
    val primaryDark: Color,
    val primaryLight: Color,
    val surface: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val divider: Color,
    val white: Color
)

fun getColorSystem(isDarkMode: Boolean): ColorSystem {
    return if (isDarkMode) {
        ColorSystem(
            accent = Color(0xFF4CAF50),
            primary = Color(0xFF673AB7),
            primaryDark = Color(0xFF512DA8),
            primaryLight = Color(0xFFD1C4E9),
            surface = Color(0xFFFFFFFF),
            primaryText = Color(0xFF212121),
            secondaryText = Color(0xFF757575),
            divider = Color(0xFFBDBDBD),
            white = Color(0xFFFFFFFF)
        )
    } else {
        ColorSystem(
            accent = Color(0xFF4CAF50),
            primary = Color(0xFF673AB7),
            primaryDark = Color(0xFF512DA8),
            primaryLight = Color(0xFFD1C4E9),
            surface = Color(0xFF757575),
            primaryText = Color(0xFFFFFFFF),
            secondaryText = Color(0xFFBDBDBD),
            divider = Color(0xFFFFFFFF),
            white = Color(0xFFFFFFFF)
        )
    }
}

val LocalColorSystem = staticCompositionLocalOf {
    ColorSystem(
        accent = Color.Unspecified,
        primary = Color.Unspecified,
        primaryDark = Color.Unspecified,
        primaryLight = Color.Unspecified,
        surface = Color.Unspecified,
        primaryText = Color.Unspecified,
        secondaryText = Color.Unspecified,
        divider = Color.Unspecified,
        white = Color.Unspecified
    )
}