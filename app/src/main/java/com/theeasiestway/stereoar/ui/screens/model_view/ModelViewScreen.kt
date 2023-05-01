package com.theeasiestway.stereoar.ui.screens.model_view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.manualcomposablecalls.ManualComposableCallsBuilder
import com.ramcosta.composedestinations.manualcomposablecalls.composable
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.theeasiestway.domain.repositories.DownloadsRepository
import com.theeasiestway.stereoar.di.modelViewScopeId
import com.theeasiestway.stereoar.ui.screens.common.compose.scaffold.TopBarAction
import com.theeasiestway.stereoar.ui.screens.common.koin.createScopeIfNull
import com.theeasiestway.stereoar.ui.screens.destinations.ModelViewScreenDestination
import com.theeasiestway.stereoar.ui.screens.model_view.ModelViewViewModel.Intent
import com.theeasiestway.stereoar.ui.screens.models_explorer.ModelUri
import kotlinx.coroutines.flow.Flow
import org.koin.androidx.compose.get
import org.koin.androidx.compose.koinViewModel

fun ManualComposableCallsBuilder.modelViewScreenFactory(
    topBarActionsClickListener: Flow<TopBarAction>
) {
    composable(ModelViewScreenDestination) {
        createScopeIfNull(scopeId = modelViewScopeId)
        ModelViewScreen(
            navigator = destinationsNavigator,
            modelUri = navArgs.modelUri,
            topBarActionsClickListener = topBarActionsClickListener
        )
    }
}

@Destination
@Composable
fun ModelViewScreen(
    navigator: DestinationsNavigator,
    modelUri: ModelUri,
    topBarActionsClickListener: Flow<TopBarAction>
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val downloadsRepository: DownloadsRepository = get()
    val viewModel: ModelViewViewModel = koinViewModel()
    val uiState = viewModel.uiState.collectAsState(initial = UiState()).value

    LaunchedEffect(Unit) {
        topBarActionsClickListener.collect { action ->
            viewModel.handleIntent(Intent.HandleTopBarActionClick(action))
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when(event) {
                Lifecycle.Event.ON_START -> {
                    downloadsRepository.startObservingDownloads()
                }
                Lifecycle.Event.ON_STOP -> {
                    downloadsRepository.stopObservingDownloads()
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    Content(uiState)
}

@Composable
private fun Content(uiState: UiState) {

}