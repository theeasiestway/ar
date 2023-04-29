package com.theeasiestway.stereoar.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Immutable
data class TypographySystem(
    val headlineLarge: TextStyle,
    val headlineMedium: TextStyle,
    val headlineSmall: TextStyle,
    val titleLarge: TextStyle,
    val titleMedium: TextStyle,
    val titleSmall: TextStyle,
    val bodyLarge: TextStyle,
    val bodyMedium: TextStyle,
    val bodySmall: TextStyle,
    val labelLarge: TextStyle,
    val labelMedium: TextStyle,
    val labelSmall: TextStyle
)

fun getTypographySystem(): TypographySystem {
    val fontFamily = FontFamily.Default
    return TypographySystem(
        headlineLarge = TextStyle(
            fontFamily = fontFamily,
            fontSize = 32.sp,
            lineHeight = 40.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = fontFamily,
            fontSize = 28.sp,
            lineHeight = 36.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = fontFamily,
            fontSize = 24.sp,
            lineHeight = 32.sp
        ),
        titleLarge = TextStyle(
            fontWeight = FontWeight.Medium,
            fontFamily = fontFamily,
            fontSize = 22.sp,
            lineHeight = 28.sp
        ),
        titleMedium = TextStyle(
            fontWeight = FontWeight.Medium,
            fontFamily = fontFamily,
            fontSize = 16.sp,
            lineHeight = 24.sp
        ),
        titleSmall = TextStyle(
            fontWeight = FontWeight.Medium,
            fontFamily = fontFamily,
            fontSize = 14.sp,
            lineHeight = 20.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = fontFamily,
            fontSize = 16.sp,
            lineHeight = 24.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = fontFamily,
            fontSize = 14.sp,
            lineHeight = 20.sp
        ),
        bodySmall = TextStyle(
            fontFamily = fontFamily,
            fontSize = 12.sp,
            lineHeight = 16.sp
        ),
        labelLarge = TextStyle(
            fontWeight = FontWeight.Medium,
            fontFamily = fontFamily,
            fontSize = 14.sp,
            lineHeight = 20.sp
        ),
        labelMedium = TextStyle(
            fontWeight = FontWeight.Medium,
            fontFamily = fontFamily,
            fontSize = 12.sp,
            lineHeight = 16.sp
        ),
        labelSmall = TextStyle(
            fontFamily = fontFamily,
            fontSize = 11.sp,
            lineHeight = 16.sp
        )
    )
}

val LocalTypographySystem = staticCompositionLocalOf {
    TypographySystem(
        headlineLarge = TextStyle(),
        headlineMedium = TextStyle(),
        headlineSmall = TextStyle(),
        titleLarge = TextStyle(),
        titleMedium = TextStyle(),
        titleSmall = TextStyle(),
        bodyLarge = TextStyle(),
        bodyMedium = TextStyle(),
        bodySmall = TextStyle(),
        labelLarge = TextStyle(),
        labelMedium = TextStyle(),
        labelSmall = TextStyle()
    )
}