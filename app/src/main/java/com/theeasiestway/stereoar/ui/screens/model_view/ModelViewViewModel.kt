package com.theeasiestway.stereoar.ui.screens.model_view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ar.sceneform.rendering.ModelRenderable
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
        object HandleSceneCleared: Intent
        data class HandlePermissionResult(
            val result: PermissionResult,
            val modelUri: ModelUri
        ): Intent
        object HandleTopBarActionClick: Intent
        data class HandleOptionsClick(val option: ModelViewOptions?): Intent
        object HandleClearScene: Intent
        data class RemoveFromCollection(val modelUri: ModelUri.File): Intent
    }

    sealed interface Event {
        object RequestPermissions: Event
        data class ShowOptions(val show: Boolean): Event
        data class ModelLoaded(
            val modelUri: ModelUri,
            val footPrintModel: ModelRenderable,
            val model: ModelRenderable,
            val addedToCollection: Boolean
        ): Event
        object ClearScene: Event
        object SceneCleared: Event
        data class SavedToCollection(val modelUri: String): Event
        data class RemovedFromCollection(val modelUri: String): Event
    }

    sealed interface SideEffect {
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
            is Intent.HandlePermissionResult -> handlePermissionResult(intent.result, intent.modelUri)
            is Intent.HandleClearScene -> handleClearScene()
            is Intent.HandleSceneCleared -> handleSceneCleared()
            is Intent.HandleTopBarActionClick -> handleTopBarActionClick()
            is Intent.HandleOptionsClick -> handleOptionsClick(modelUri = state.modelUri, option = intent.option)
            is Intent.RemoveFromCollection -> removeFromCollection(intent.modelUri)
        }
    }

    private fun requestPermissions() = flow<Event> {
        emit(Event.RequestPermissions)
    }

    private fun handlePermissionResult(
        result: PermissionResult,
        modelUri: ModelUri
    ): Flow<Event> {
        return when(result) {
            PermissionResult.Granted -> loadModel(modelUri)
            PermissionResult.DeniedForeverAndCanceled -> {
                postSideEffect(SideEffect.CloseScreen)
                emptyFlow()
            }
        }
    }

    private fun handleClearScene() = flow<Event> {
        emit(Event.ClearScene)
    }

    private fun handleSceneCleared() = flow<Event> {
        emit(Event.SceneCleared)
    }

    private fun handleTopBarActionClick() = flow<Event> {
        emit(Event.ShowOptions(show = true))
    }

    private fun handleOptionsClick(
        modelUri: ModelUri?,
        option: ModelViewOptions?
    ) = flow<Event> {
        emit(Event.ShowOptions(show = false))
        when(option) {
            ModelViewOptions.AddToCollection -> modelUri?.let { saveToCollection(modelUri) }
            ModelViewOptions.AppSettings -> postSideEffect(SideEffect.OpenAppSettings)
            null -> Unit
        }
    }

    private suspend fun saveToCollection(modelUri: ModelUri) {
        try {
            val savedName = filesRepository.saveModelToCollection(modelUri.toFileUri())
            postSideEffect(SideEffect.SavedToCollection(savedName.substringAfterLast("/")))
        } catch (e: Throwable) {
            e.printStackTrace()
            postSideEffect(SideEffect.ErrorSaveToCollection)
        }
    }

    private fun loadModel(modelUri: ModelUri) = channelFlow<Event> {
        viewModelScope.launch(dispatcherIO) {
            try {
                coroutineScope {
                    val collectedModelsDeferred = async {
                        filesRepository.loadModelsFromCollection().map { file ->
                            file.absolutePath
                        }
                    }
                    val footPrintModelDeferred = async(dispatcherMain) {
                        modelsRepository.loadFootPrintModel()
                    }
                    val modelDeferred = async(dispatcherMain) {
                        modelsRepository.loadModel(modelUri.uri)!!
                    }
                    val collectedModels = collectedModelsDeferred.await()
                    val footPrintModel = footPrintModelDeferred.await()
                    val model = modelDeferred.await()
                    send(
                        Event.ModelLoaded(
                            modelUri = modelUri,
                            footPrintModel = footPrintModel,
                            model = model,
                            addedToCollection = collectedModels.contains(modelUri.uri)
                        )
                    )
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                postSideEffect(SideEffect.ErrorLoadingModel)
            }
        }.join()
    }

    private fun removeFromCollection(modelUri: ModelUri.File) = flow<Event> {
        try {
            filesRepository.removeModelFromCollection(modelUri.toFileUri())
            postSideEffect(SideEffect.RemovedFromCollection(modelUri.getFileName()))
        } catch (e: Throwable) {
            e.printStackTrace()
            postSideEffect(SideEffect.ErrorRemoveFromCollection)
        }
    }

    private fun reduce(state: State, event: Event): State {
        return when(event) {
            is Event.RequestPermissions -> state.copy(
                requestPermissions = true
            )
            is Event.ShowOptions -> state.copy(
                showOptions = event.show
            )
            is Event.ModelLoaded -> state.copy(
                isLoading = false,
                requestPermissions = false,
                addedToCollection = event.addedToCollection,
                modelUri = event.modelUri,
                footPrintModel = event.footPrintModel,
                model = event.model
            )
            is Event.ClearScene -> state.copy(
                clearScene = true
            )
            is Event.SceneCleared -> state.copy(
                clearScene = false
            )
            is Event.RemovedFromCollection -> state.copy(
                isLoading = false,
                addedToCollection = false
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
        addedToCollection = addedToCollection,
        footPrintModel = footPrintModel,
        model = model,
        showOptions = showOptions,
        clearScene = clearScene
    )
}