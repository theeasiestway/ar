package com.theeasiestway.stereoar.ui.screens.model_view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ar.sceneform.rendering.ModelRenderable
import com.theeasiestway.domain.model.CollectedModel
import com.theeasiestway.domain.repositories.FilesRepository
import com.theeasiestway.domain.repositories.ModelsRepository
import com.theeasiestway.stereoar.di.modelViewScopeId
import com.theeasiestway.stereoar.ui.screens.common.compose.permissions.PermissionResult
import com.theeasiestway.stereoar.ui.screens.common.ext.postSideEffect
import com.theeasiestway.stereoar.ui.screens.common.ext.state
import com.theeasiestway.stereoar.ui.screens.common.koin.closeScope
import com.theeasiestway.stereoar.ui.screens.models_explorer.ModelUri
import com.theeasiestway.stereoar.ui.screens.models_explorer.toFileUri
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container

class ModelViewViewModel(
    private val modelsRepository: ModelsRepository<ModelRenderable>,
    private val filesRepository: FilesRepository,
    private val dispatcherIO: CoroutineDispatcher,
    private val dispatcherMain: CoroutineDispatcher
): ContainerHost<State, ModelViewViewModel.SideEffect>, ViewModel(), KoinComponent {

    override val container = container<State, SideEffect>(State()) {
        handleIntent(Intent.RequestPermissions)
    }
    val uiState = state.map { it.toUiState() }

    sealed interface Intent {
        object RequestPermissions: Intent
        data class LoadModel(val modelUri: ModelUri, val loadingText: String): Intent
        object HandleSceneCleared: Intent
        data class HandlePermissionResult(val result: PermissionResult): Intent
        object HandleTopBarActionClick: Intent
        data class HandleOptionsClick(val option: ModelViewOptions?): Intent

        object HandleClearScene: Intent
        object HandleBackClick: Intent
    }

    sealed interface Event {
        object RequestPermissions: Event
        data class ShowOptions(val options: List<ModelViewOptions>): Event
        data class ModelLoaded(
            val modelUri: ModelUri.File,
            val footPrintModel: ModelRenderable,
            val model: ModelRenderable,
            val addedToCollection: Boolean,
            val collectedModels: List<CollectedModel>
        ): Event
        object ClearScene: Event
        object SceneCleared: Event
        data class SavedToCollection(val modelUri: String): Event
    }

    sealed interface SideEffect {
        object LoadModel: SideEffect
        object CloseScreen: SideEffect
        object OpenAppSettings: SideEffect
        data class SavedToCollection(val modelName: String): SideEffect
        data class RemovedFromCollection(val modelName: String): SideEffect
        object ErrorSaveToCollection: SideEffect
        object ErrorRemoveFromCollection: SideEffect
        object ErrorLoadingModel: SideEffect
    }

    fun handleIntent(intent: Intent) {
        intent {
            actor(state, intent).collect { event ->
                reduce { reduce(state, event) }
            }
        }
    }

    private fun actor(state: State, intent: Intent): Flow<Event> {
        return when(intent) {
            is Intent.RequestPermissions -> requestPermissions()
            is Intent.LoadModel -> loadModel(modelUri = intent.modelUri, loadingText = intent.loadingText)
            is Intent.HandlePermissionResult -> handlePermissionResult(result = intent.result)
            is Intent.HandleClearScene -> handleClearScene()
            is Intent.HandleSceneCleared -> handleSceneCleared()
            is Intent.HandleTopBarActionClick -> handleTopBarActionClick(modelUri = state.modelUri!!, addedToCollection = state.addedToCollection)
            is Intent.HandleOptionsClick -> handleOptionsClick(option = intent.option)
            is Intent.HandleBackClick -> handleBackClick()
        }
    }

    private fun handleBackClick(): Flow<Event> {
        postSideEffect(SideEffect.CloseScreen)
        return emptyFlow()
    }

    private fun requestPermissions() = flow<Event> {
        emit(Event.RequestPermissions)
    }

    private fun handlePermissionResult(
        result: PermissionResult
    ): Flow<Event> {
        postSideEffect(
            when(result) {
                PermissionResult.Granted -> SideEffect.LoadModel
                PermissionResult.DeniedForeverAndCanceled -> SideEffect.CloseScreen
            }
        )
        return emptyFlow()
    }

    private fun handleClearScene() = flow<Event> {
        emit(Event.ClearScene)
    }

    private fun handleSceneCleared() = flow<Event> {
        emit(Event.SceneCleared)
    }

    private fun handleTopBarActionClick(modelUri: ModelUri.File, addedToCollection: Boolean) = flow<Event> {
        emit(
            Event.ShowOptions(
                mutableListOf<ModelViewOptions>(
                    ModelViewOptions.AppSettings
                ).apply {
                    if (!addedToCollection) {
                        add(0, ModelViewOptions.SaveToCollection(modelUri))
                    }
                }
            )
        )
    }

    private fun handleOptionsClick(option: ModelViewOptions?) = flow<Event> {
        emit(Event.ShowOptions(options = emptyList()))
        when(option) {
            is ModelViewOptions.SaveToCollection -> {
                saveToCollection(modelUri = option.modelUri)
            }
            is ModelViewOptions.AppSettings -> {
                postSideEffect(SideEffect.OpenAppSettings)
            }
            null -> Unit
        }
    }

    private suspend fun saveToCollection(modelUri: ModelUri.File) {
        try {
            val savedName = filesRepository.saveModelToCollection(
                fileUri = modelUri.toFileUri()
            )
            postSideEffect(SideEffect.SavedToCollection(savedName.substringAfterLast("/")))
        } catch (e: Throwable) {
            e.printStackTrace()
            postSideEffect(SideEffect.ErrorSaveToCollection)
        }
    }

    private fun loadModel(modelUri: ModelUri, loadingText: String) = channelFlow<Event> {
        viewModelScope.launch(dispatcherIO) {
            try {
                var modelFileUri: ModelUri.File? = null
                coroutineScope {
                    val collectedModelsDeferred = async {
                        filesRepository.loadModelsFromCollection()
                    }
                    val footPrintModelDeferred = async(dispatcherMain) {
                        modelsRepository.loadFootPrintModel()
                    }
                    val modelDeferred = async(dispatcherMain) {
                        if (modelUri is ModelUri.File) {
                            modelFileUri = modelUri
                            modelsRepository.loadModel(modelUri.uri)!!
                        } else {
                            val downloadedModelUri = filesRepository.downloadModel(
                                modelUri = modelUri.uri,
                                loadingTitle = loadingText
                            )
                            modelFileUri = ModelUri.File(downloadedModelUri)
                            modelsRepository.loadModel(downloadedModelUri)!!
                        }
                    }
                    val collectedModels = collectedModelsDeferred.await()
                    val footPrintModel = footPrintModelDeferred.await()
                    val model = modelDeferred.await()
                    send(
                        Event.ModelLoaded(
                            modelUri = modelFileUri!!,
                            footPrintModel = footPrintModel,
                            model = model,
                            addedToCollection = collectedModels.any { collectedModel ->
                                collectedModel.path == modelUri.uri
                            },
                            collectedModels = collectedModels
                        )
                    )
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                postSideEffect(SideEffect.ErrorLoadingModel)
            }
        }.join()
    }

    private fun reduce(state: State, event: Event): State {
        return when(event) {
            is Event.RequestPermissions -> state.copy(
                requestPermissions = true
            )
            is Event.ShowOptions -> state.copy(
                options = event.options
            )
            is Event.ModelLoaded -> state.copy(
                isLoading = false,
                requestPermissions = false,
                addedToCollection = event.addedToCollection,
                modelUri = event.modelUri,
                footPrintModel = event.footPrintModel,
                model = event.model,
                collectedModels = event.collectedModels
            )
            is Event.ClearScene -> state.copy(
                clearScene = true
            )
            is Event.SceneCleared -> state.copy(
                clearScene = false
            )
            is Event.SavedToCollection -> state.copy(
                isLoading = false,
                addedToCollection = true
            )
        }
    }

    override fun onCleared() {
        closeScope(modelViewScopeId)
    }
}

private fun State.toUiState(): UiState {
    return UiState(
        isLoading = isLoading,
        requestPermissions = requestPermissions,
        footPrintModel = footPrintModel,
        model = model,
        options = options,
        clearScene = clearScene
    )
}