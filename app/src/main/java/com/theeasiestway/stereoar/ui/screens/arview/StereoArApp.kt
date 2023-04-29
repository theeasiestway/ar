package com.theeasiestway.stereoar.ui.screens.arview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.theeasiestway.stereoar.ui.screens.NavGraphs
import com.theeasiestway.stereoar.ui.screens.choose_model.viewmodel.ChooseArModelViewModel
import com.theeasiestway.stereoar.ui.screens.arview.navigation.Screens
import com.theeasiestway.stereoar.ui.screens.common.compose.scaffold.AppScaffold
import com.theeasiestway.stereoar.ui.screens.destinations.ModelsExplorerScreenDestination
import com.theeasiestway.stereoar.ui.theme.AppTheme

/**
 * Created by Alexey Loboda on 04.09.2022
 */

@Composable
fun StereoArApp() {
    AppTheme {
        AppScaffold(
            navGraph = NavGraphs.root,
            startRoute = ModelsExplorerScreenDestination
        )
    }
}