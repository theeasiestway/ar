package com.theeasiestway.stereoar.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class ShapeSystem(
    val none: Dp,
    val extraSmall: Dp,
    val small: Dp,
    val medium: Dp,
    val large: Dp,
    val extraLarge: Dp
)

fun getShapeSystem(): ShapeSystem {
    return ShapeSystem(
        none = 0.dp,
        extraSmall = 4.dp,
        small = 8.dp,
        medium = 12.dp,
        large = 16.dp,
        extraLarge = 28.dp
    )
}

val LocalShapeSystem = staticCompositionLocalOf {
    ShapeSystem(
        none = 0.dp,
        extraSmall = 0.dp,
        small = 0.dp,
        medium = 0.dp,
        large = 0.dp,
        extraLarge = 0.dp
    )
}