package com.theeasiestway.stereoar.ui.screens.common.compose.scaffold

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.theeasiestway.stereoar.R
import com.theeasiestway.stereoar.ui.screens.common.compose.buttons.TopBarButton
import com.theeasiestway.stereoar.ui.screens.common.compose.text.TitleMedium
import com.theeasiestway.stereoar.ui.screens.common.ext.resource
import com.theeasiestway.stereoar.ui.screens.destinations.Destination
import com.theeasiestway.stereoar.ui.screens.destinations.ModelViewScreenDestination
import com.theeasiestway.stereoar.ui.screens.destinations.ModelsExplorerScreenDestination
import com.theeasiestway.stereoar.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    destination: Destination,
    onActionClick: (TopBarAction) -> Unit
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = AppTheme.colors.primary,
            navigationIconContentColor = AppTheme.colors.white,
            actionIconContentColor = AppTheme.colors.white
        ),
        title = {
            TitleMedium(
                text = destination.toText(),
                color = AppTheme.colors.white
            )
        },
        actions = { destination.toActions(onActionClick) }
    )
}

enum class TopBarAction {
    More
}

@Composable
private fun Destination.toActions(onActionClick: (TopBarAction) -> Unit) {
    when(this) {
        is ModelsExplorerScreenDestination -> {
            TopBarButton(
                icon = R.drawable.ic_more,
                tint = AppTheme.colors.surface
            ) {
                onActionClick(TopBarAction.More)
            }
        }
        else -> {}
    }
}

@Composable
private fun Destination.toText(): String {
    return when(this) {
        is ModelsExplorerScreenDestination,
        is ModelViewScreenDestination -> R.string.app_name.resource()
        else -> TODO()
    }
}