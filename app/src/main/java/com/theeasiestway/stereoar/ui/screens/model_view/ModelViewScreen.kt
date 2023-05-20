package com.theeasiestway.stereoar.ui.screens.model_view

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.manualcomposablecalls.ManualComposableCallsBuilder
import com.ramcosta.composedestinations.manualcomposablecalls.composable
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.ResultBackNavigator
import com.ramcosta.composedestinations.scope.resultBackNavigator
import com.theeasiestway.domain.repositories.DownloadsRepository
import com.theeasiestway.stereoar.R
import com.theeasiestway.stereoar.di.modelViewScopeId
import com.theeasiestway.stereoar.ui.screens.common.compose.buttons.TopBarButton
import com.theeasiestway.stereoar.ui.screens.common.compose.custom.BubblesEffect
import com.theeasiestway.stereoar.ui.screens.common.compose.custom.MenuContainer
import com.theeasiestway.stereoar.ui.screens.common.compose.items.PopupItem
import com.theeasiestway.stereoar.ui.screens.common.compose.permissions.PermissionResult
import com.theeasiestway.stereoar.ui.screens.common.compose.permissions.RequestCameraPermission
import com.theeasiestway.stereoar.ui.screens.common.compose.text.BodyLarge
import com.theeasiestway.stereoar.ui.screens.common.ext.onSideEffect
import com.theeasiestway.stereoar.ui.screens.common.ext.resource
import com.theeasiestway.stereoar.ui.screens.common.ext.showSnackBar
import com.theeasiestway.stereoar.ui.screens.common.koin.createScopeIfNull
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
) {
    composable(ModelViewScreenDestination) {
        createScopeIfNull(scopeId = modelViewScopeId)
        ModelViewScreen(
            navigator = destinationsNavigator,
            resultNavigator = resultBackNavigator(),
            modelUri = navArgs.modelUri
        )
    }
}

@Destination
@Composable
fun ModelViewScreen(
    navigator: DestinationsNavigator,
    resultNavigator: ResultBackNavigator<ModelViewResult>,
    modelUri: ModelUri
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackBarHostState = remember { SnackbarHostState() }
    val downloadsRepository: DownloadsRepository = get()
    val viewModel: ModelViewViewModel = koinViewModel()
    val uiState = viewModel.uiState.collectAsState(initial = UiState()).value
    var navResult by remember { mutableStateOf<ModelViewResult?>(null) }

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
            is SideEffect.LoadModel -> {
                val loadingText = context.getString(R.string.model_view_downloading_model_to_collection)
                viewModel.handleIntent(Intent.LoadModel(modelUri = modelUri, loadingText = loadingText))
            }
            is SideEffect.OpenAppSettings -> {
                showSnackBar(
                    coroutineScope = coroutineScope,
                    snackBarHostState = snackBarHostState,
                    message = "TODO"
                )
            }
            is SideEffect.CloseScreen -> {
                navResult?.let { result ->
                    resultNavigator.navigateBack(result = result)
                } ?: navigator.popBackStack()
            }
            is SideEffect.SavedToCollection -> {
                navResult = ModelViewResult.CollectedModelsChanged
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
                ) {
                    navigator.popBackStack()
                }
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

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        }
    ) { paddingValues ->
        Content(
            isLocalModel = modelUri is ModelUri.File,
            uiState = uiState,
            modifier = Modifier.padding(paddingValues),
            onRequestPermissionResult = { result ->
                viewModel.handleIntent(Intent.HandlePermissionResult(result = result))
            },
            onTopBarOptionsClick = {
                viewModel.handleIntent(Intent.HandleTopBarActionClick)
            },
            onOptionsClick = { option ->
                viewModel.handleIntent(Intent.HandleOptionsClick(option))
            },
            onSceneCleared = {
                viewModel.handleIntent(Intent.HandleSceneCleared)
            },
            onBackClick = {
                viewModel.handleIntent(Intent.HandleBackClick)
            }
        )
    }
}

@Composable
private fun Content(
    isLocalModel: Boolean,
    uiState: UiState,
    modifier: Modifier,
    onRequestPermissionResult: (PermissionResult) -> Unit,
    onTopBarOptionsClick: () -> Unit,
    onOptionsClick: (ModelViewOptions?) -> Unit,
    onSceneCleared: () -> Unit,
    onBackClick: () -> Unit
) {
    when {
        uiState.isLoading || uiState.requestPermissions -> {
            if (uiState.requestPermissions) {
                CameraPermissionDialog(onRequestPermissionResult)
            }
            ShimmeredScene(
                isLocalModel = isLocalModel,
                modifier = modifier
            )
        }
        else -> {
            if (uiState.options.isNotEmpty()) {
                TopBarOptions(
                    options = uiState.options,
                    onOptionsClick = onOptionsClick
                )
            }
            Scene(
                modifier = modifier,
                sceneState = uiState.toSceneState(),
                onSceneCleared = onSceneCleared,
                onTopBarOptionsClick = onTopBarOptionsClick,
                onBackClick = onBackClick
            )
        }
    }
}

@Composable
fun CameraPermissionDialog(onRequestPermissionResult: (PermissionResult) -> Unit) {
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

@Composable
private fun Scene(
    sceneState: ArSceneState,
    modifier: Modifier = Modifier,
    onTopBarOptionsClick: () -> Unit,
    onSceneCleared: () -> Unit,
    onBackClick: () -> Unit
) {
    BackHandler(onBack = onBackClick)
    Box(
        modifier = modifier,
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
            onClick = onTopBarOptionsClick
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
private fun ShimmeredScene(
    isLocalModel: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        BubblesEffect(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            bubblesCount = 20
        )
        LoadingText(isLocalModel)
    }
}

@Composable
private fun LoadingText(
    isLocalModel: Boolean
) {
    val animation = remember { Animatable(0f) }
    val textPostfix = when(animation.value.toInt()) {
        0 -> ""
        1 -> "."
        2 -> ".."
        else -> "..."
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        BodyLarge(
            text = if (isLocalModel) R.string.model_view_loading_model.resource()
            else R.string.model_view_downloading_model.resource(),
            color = AppTheme.colors.surface
        )
        BodyLarge(
            modifier = Modifier.width(20.dp),
            text = textPostfix,
            color = AppTheme.colors.surface
        )
    }
    LaunchedEffect(animation) {
        animation.animateTo(
            targetValue = 4f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 2500,
                    easing = LinearEasing
                )
            )
        )
    }
}

@Composable
private fun TopBarOptions(
    options: List<ModelViewOptions>,
    onOptionsClick: (ModelViewOptions?) -> Unit
) {
    Popup(
        alignment = Alignment.TopEnd,
        onDismissRequest = {
            onOptionsClick(null)
        }
    ) {
        MenuContainer {
            options.forEach { option ->
                when (option) {
                    is ModelViewOptions.SaveToCollection -> {
                        PopupItem(text = R.string.model_view_save_to_collection.resource()) {
                            onOptionsClick(ModelViewOptions.SaveToCollection(option.modelUri))
                        }
                        Divider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = DividerDefaults.color.copy(alpha = 0.1f)
                        )
                    }
                    is ModelViewOptions.AppSettings -> {
                        PopupItem(text = R.string.general_app_settings.resource()) {
                            onOptionsClick(ModelViewOptions.AppSettings)
                        }
                    }
                }
            }
        }
    }
}