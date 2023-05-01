package com.theeasiestway.stereoar.ui.screens.model_view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.manualcomposablecalls.ManualComposableCallsBuilder
import com.ramcosta.composedestinations.manualcomposablecalls.composable
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.theeasiestway.domain.repositories.DownloadsRepository
import com.theeasiestway.stereoar.R
import com.theeasiestway.stereoar.di.modelViewScopeId
import com.theeasiestway.stereoar.ui.screens.common.compose.buttons.TopBarButton
import com.theeasiestway.stereoar.ui.screens.common.compose.permissions.PermissionResult
import com.theeasiestway.stereoar.ui.screens.common.compose.permissions.RequestCameraPermission
import com.theeasiestway.stereoar.ui.screens.common.ext.resource
import com.theeasiestway.stereoar.ui.screens.common.ext.showSnackBar
import com.theeasiestway.stereoar.ui.screens.common.koin.createScopeIfNull
import com.theeasiestway.stereoar.ui.screens.common.onSideEffect
import com.theeasiestway.stereoar.ui.screens.destinations.ModelViewScreenDestination
import com.theeasiestway.stereoar.ui.screens.model_view.ModelViewViewModel.Intent
import com.theeasiestway.stereoar.ui.screens.model_view.ModelViewViewModel.SideEffect
import com.theeasiestway.stereoar.ui.screens.model_view.ar_scene.ArScene
import com.theeasiestway.stereoar.ui.screens.model_view.ar_scene.ArSceneState
import com.theeasiestway.stereoar.ui.screens.models_explorer.ModelUri
import com.theeasiestway.stereoar.ui.theme.AppTheme
import org.koin.androidx.compose.get
import org.koin.androidx.compose.koinViewModel

fun ManualComposableCallsBuilder.modelViewScreenFactory(
    snackBarHostState: SnackbarHostState
) {
    composable(ModelViewScreenDestination) {
        createScopeIfNull(scopeId = modelViewScopeId)
        ModelViewScreen(
            snackBarHostState = snackBarHostState,
            navigator = destinationsNavigator,
            modelUri = navArgs.modelUri,
        )
    }
}

@Destination
@Composable
fun ModelViewScreen(
    snackBarHostState: SnackbarHostState,
    navigator: DestinationsNavigator,
    modelUri: ModelUri
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val downloadsRepository: DownloadsRepository = get()
    val viewModel: ModelViewViewModel = koinViewModel()
    val uiState = viewModel.uiState.collectAsState(initial = UiState()).value

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

    viewModel.onSideEffect { effect ->
        when(effect) {
            is SideEffect.CloseScreen -> {
                navigator.popBackStack()
            }
            is SideEffect.SavedToCollection -> {
                showSnackBar(
                    coroutineScope,
                    snackBarHostState,
                    context.getString(R.string.model_view_model_saved_to_collection, effect.modelName)
                )
            }
            is SideEffect.RemovedFromCollection -> {
                showSnackBar(
                    coroutineScope,
                    snackBarHostState,
                    context.getString(R.string.model_view_model_removed_from_collection, effect.modelName)
                )
            }
            is SideEffect.ErrorLoadingModel -> {
                showSnackBar(
                    coroutineScope,
                    snackBarHostState,
                    context.getString(R.string.model_view_error_loading_model)
                )
            }
            is SideEffect.ErrorRemoveFromCollection -> {
                showSnackBar(
                    coroutineScope,
                    snackBarHostState,
                    context.getString(R.string.model_view_error_remove_model_from_collection)
                )
            }
            is SideEffect.ErrorSaveToCollection -> {
                showSnackBar(
                    coroutineScope,
                    snackBarHostState,
                    context.getString(R.string.model_view_error_save_model_to_collection)
                )
            }
        }
    }

    Content(
        uiState = uiState,
        onRequestPermissionResult = { result ->
            viewModel.handleIntent(
                Intent.HandlePermissionResult(
                    result = result,
                    modelUri = modelUri
                )
            )
        },
        onOptionsClick = {
            viewModel.handleIntent(Intent.HandleTopBarActionClick)
        },
        onSceneCleared = {
            viewModel.handleIntent(Intent.HandleSceneCleared)
        }
    )
}

@Composable
private fun Content(
    uiState: UiState,
    onRequestPermissionResult: (PermissionResult) -> Unit,
    onOptionsClick: () -> Unit,
    onSceneCleared: () -> Unit
) {
    if (uiState.isLoading || uiState.requestPermissions) {
        if (uiState.requestPermissions) {
            RequestCameraPermission(
                icon = R.drawable.ic_camera,
                rationalTitle = R.string.camera_permission_rational_title.resource(),
                rationalText = R.string.camera_permission_rational_text.resource(),
                deniedTitle = R.string.camera_permission_rational_title.resource(),
                deniedText = R.string.camera_permission_denied_text.resource(),
                deniedDismissButtonText = R.string.general_cancel.resource(),
                onResult = onRequestPermissionResult
            )
        }
        ShimmeredScene()
    } else {
        Scene(
            sceneState = uiState.toSceneState(),
            onOptionsClick = onOptionsClick,
            onSceneCleared = onSceneCleared
        )
    }
}

@Composable
private fun Scene(
    sceneState: ArSceneState,
    onOptionsClick: () -> Unit,
    onSceneCleared: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopEnd
    ) {
        ArScene(
            modifier = Modifier.fillMaxSize(),
            state = sceneState,
            onSceneCleared = onSceneCleared
        )
        TopBarButton(
            icon = R.drawable.ic_more,
            tint = AppTheme.colors.surface,
            onClick = onOptionsClick
        )
    }
}

private fun UiState.toSceneState(): ArSceneState {
    return ArSceneState(
        footPrintModel = footPrintModel!!,
        model = model!!,
        clearScene = clearScene,
    )
}

@Composable
private fun ShimmeredScene() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopEnd
    ) {
        ShimmeredArScene()
        TopBarButton(
            icon = R.drawable.ic_more,
            tint = AppTheme.colors.surface,
            onClick = {}
        )
    }
}

@Composable
private fun ShimmeredArScene() {

}