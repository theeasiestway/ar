package com.theeasiestway.stereoar.ui.screens.common.compose.palette

import androidx.compose.ui.graphics.Color

object MaterialPalette500 {
    val red: Color = Color(0xFFF44336)
    val pink: Color = Color(0xFFE91E63)
    val purple: Color = Color(0xFF9C27B0)
    val violet: Color = Color(0xFF673AB7)
    val darkBlue: Color = Color(0xFF3F51B5)
    val blue: Color = Color(0xFF2196F3)
    val lightBlue: Color = Color(0xFF00BCD4)
    val darkGreen: Color = Color(0xFF009688)
    val green: Color = Color(0xFF4CAF50)
    val lightGreen: Color = Color(0xFF8BC34A)
    val darkYellow: Color = Color(0xFFCDDC39)
    val yellow: Color = Color(0xFFFFEB3B)
    val lightOrange: Color = Color(0xFFFFC107)
    val orange: Color = Color(0xFFFF9800)
    val darkOrange: Color = Color(0xFFFF5722)

    fun randomColor(): Color {
        return when((0..14).random()) {
            0 -> red
            1 -> pink
            2 -> purple
            3 -> violet
            4 -> darkBlue
            5 -> blue
            6 -> lightBlue
            7 -> darkGreen
            8 -> green
            9 -> lightGreen
            10 -> darkYellow
            11 -> yellow
            12 -> lightOrange
            13 -> orange
            else -> darkOrange
        }
    }
}