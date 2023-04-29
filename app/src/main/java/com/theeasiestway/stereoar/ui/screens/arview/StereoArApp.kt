package com.theeasiestway.stereoar.ui.screens.arview

import androidx.compose.runtime.Composable
import com.theeasiestway.stereoar.ui.screens.NavGraphs
import com.theeasiestway.stereoar.ui.screens.common.compose.scaffold.AppScaffold
import com.theeasiestway.stereoar.ui.screens.destinations.ModelsExplorerScreenDestination
import com.theeasiestway.stereoar.ui.theme.AppTheme

/**
 * Created by Alexey Loboda on 04.09.2022
 */

@Composable
fun StereoArApp(
    onCloseApp: () -> Unit
) {
    AppTheme {
        AppScaffold(
            navGraph = NavGraphs.root,
            startRoute = ModelsExplorerScreenDestination,
            onCloseApp = onCloseApp
        )
    }
}