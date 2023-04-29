package com.theeasiestway.stereoar.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    val colorSystem = getColorSystem(isSystemInDarkTheme())
    val typographySystem = getTypographySystem()
    val shapeSystem = getShapeSystem()

    CompositionLocalProvider(
        LocalColorSystem provides colorSystem,
        LocalTypographySystem provides typographySystem,
        LocalShapeSystem provides shapeSystem,
        content = content
    )
}

object AppTheme {
    val colors: ColorSystem
    @Composable
    get() = LocalColorSystem.current

    val typography: TypographySystem
    @Composable
    get() = LocalTypographySystem.current

    val shapes: ShapeSystem
    @Composable
    get() = LocalShapeSystem.current
}