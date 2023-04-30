package com.theeasiestway.stereoar.ui.screens.model_view

import androidx.compose.runtime.Composable
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.theeasiestway.stereoar.ui.screens.common.compose.scaffold.TopBarAction
import kotlinx.coroutines.flow.Flow

@Destination
@Composable
fun ModelViewScreen(
    navigator: DestinationsNavigator,
    topBarActionsClickListener: Flow<TopBarAction>
) {

}