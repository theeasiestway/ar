package com.theeasiestway.stereoar.ui.screens.common.compose.custom

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.theeasiestway.stereoar.ui.theme.AppTheme

@Composable
fun MenuContainer(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .padding(16.dp)
            .widthIn(max = 180.dp),
        shadowElevation = 3.dp,
        tonalElevation = 3.dp,
        shape = RoundedCornerShape(AppTheme.shapes.medium),
        color = AppTheme.colors.surface
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}