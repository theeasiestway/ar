package com.theeasiestway.stereoar.ui.screens.common.compose.scaffold

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.theeasiestway.stereoar.ui.screens.common.compose.text.TitleMedium
import com.theeasiestway.stereoar.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String,
    containerColor: Color = AppTheme.colors.primary,
    navigationIconContentColor: Color = AppTheme.colors.white,
    actionIconContentColor: Color = AppTheme.colors.white,
    actions: @Composable RowScope.() -> Unit
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor,
            navigationIconContentColor = navigationIconContentColor,
            actionIconContentColor = actionIconContentColor
        ),
        title = {
            TitleMedium(
                text = title,
                color = AppTheme.colors.white
            )
        },
        actions = actions
    )
}