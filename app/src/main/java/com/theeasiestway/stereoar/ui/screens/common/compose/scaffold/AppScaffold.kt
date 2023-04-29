package com.theeasiestway.stereoar.ui.screens.common.compose.scaffold

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.plusAssign
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.defaults.NestedNavGraphDefaultAnimations
import com.ramcosta.composedestinations.animations.defaults.RootNavGraphDefaultAnimations
import com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine
import com.ramcosta.composedestinations.spec.Route
import com.theeasiestway.stereoar.ui.screens.NavGraph
import com.theeasiestway.stereoar.ui.screens.NavGraphs
import com.theeasiestway.stereoar.ui.screens.appCurrentDestinationAsState
import com.theeasiestway.stereoar.ui.screens.models_explorer.modelsExplorerScreenFactory
import com.theeasiestway.stereoar.ui.screens.startAppDestination
import com.theeasiestway.stereoar.ui.theme.AppTheme
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow

@OptIn(
    ExperimentalMaterialNavigationApi::class,
    ExperimentalAnimationApi::class
)
@Composable
fun AppScaffold(
    navGraph: NavGraph,
    startRoute: Route,
    onCloseApp: () -> Unit
) {
    val navHostEngine = rememberAnimatedNavHostEngine(
        rootDefaultAnimations = RootNavGraphDefaultAnimations.ACCOMPANIST_FADING,
        defaultAnimationsForNestedNavGraph = mapOf(
            NavGraphs.root to NestedNavGraphDefaultAnimations(
                enterTransition = { fadeIn(animationSpec = tween(300)) },
                exitTransition = { fadeOut(animationSpec = tween(300)) }
            )
        )
    )
    val navController: NavHostController = navHostEngine.rememberNavController()
    val bottomSheetNavigator = rememberBottomSheetNavigator()
    navController.navigatorProvider += bottomSheetNavigator

    val destination = navController.appCurrentDestinationAsState().value ?: startRoute.startAppDestination
    val coroutineScope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }
    val topBarActionsClickListener by remember {
        mutableStateOf(
            MutableSharedFlow<TopBarAction>(
                extraBufferCapacity = 1,
                onBufferOverflow = BufferOverflow.DROP_OLDEST
            )
        )
    }

    ModalBottomSheetLayout(
        modifier = Modifier.systemBarsPadding(),
        scrimColor = Color.Black.copy(alpha = 0.4f),
        bottomSheetNavigator = bottomSheetNavigator,
        sheetShape = RoundedCornerShape(
            topStart = AppTheme.shapes.extraLarge,
            topEnd = AppTheme.shapes.extraLarge
        )
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
            topBar = {
                TopBar(destination = destination) { action ->
                    topBarActionsClickListener.tryEmit(action)
                }
            },
        ) { paddingValues ->
            DestinationsNavHost(
                modifier = Modifier.padding(paddingValues),
                engine = navHostEngine,
                navController = navController,
                navGraph = navGraph,
                startRoute = startRoute
            ) {
                modelsExplorerScreenFactory(
                    snackBarHostState = snackBarHostState,
                    topBarActionsClickListener = topBarActionsClickListener,
                    onCloseApp = onCloseApp
                )
            }
        }
    }
}