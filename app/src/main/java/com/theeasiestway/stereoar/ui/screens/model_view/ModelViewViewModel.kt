package com.theeasiestway.stereoar.ui.screens.model_view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ar.sceneform.rendering.ModelRenderable
import com.theeasiestway.domain.repositories.FilesRepository
import com.theeasiestway.domain.repositories.ModelsRepository
import com.theeasiestway.stereoar.di.modelViewScopeId
import com.theeasiestway.stereoar.ui.screens.common.compose.scaffold.TopBarAction
import com.theeasiestway.stereoar.ui.screens.common.koin.closeScope
import com.theeasiestway.stereoar.ui.screens.common.postSideEffect
import com.theeasiestway.stereoar.ui.screens.common.state
import com.theeasiestway.stereoar.ui.screens.models_explorer.ModelUri
import com.theeasiestway.stereoar.ui.screens.models_explorer.toFileUri
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container

class ModelViewViewModel(
    private val modelsRepository: ModelsRepository<ModelRenderable>,
    private val filesRepository: FilesRepository,
    private val dispatcher: CoroutineDispatcher
): ContainerHost<State, ModelViewViewModel.SideEffect>, ViewModel(), KoinComponent {

    override val container = container<State, SideEffect>(State())
    val uiState = state.map { it.toUiState() }

    sealed interface Intent {
        data class HandleTopBarActionClick(val action: TopBarAction): Intent
        data class LoadModel(val modelUri: ModelUri): Intent
        data class SaveToCollection(val modelUri: ModelUri): Intent
        data class RemoveFromCollection(val modelUri: ModelUri.File): Intent
    }

    sealed interface Event {
        object ShowOptions: Event
        data class ModelLoaded(
            val model: ModelRenderable,
            val addedToCollection: Boolean
        ): Event
        data class SavedToCollection(val modelUri: String): Event
        data class RemovedFromCollection(val modelUri: String): Event
    }

    sealed interface SideEffect {
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
            is Intent.HandleTopBarActionClick -> handleTopBarActionClick(intent.action)
            is Intent.LoadModel -> loadModel(intent.modelUri)
            is Intent.SaveToCollection -> saveToCollection(intent.modelUri)
            is Intent.RemoveFromCollection -> removeFromCollection(intent.modelUri)
        }
    }

    private fun handleTopBarActionClick(action: TopBarAction): Flow<Event> {
        return if (action == TopBarAction.More) showOptions()
        else emptyFlow()
    }

    private fun showOptions() = flow<Event> {
        emit(Event.ShowOptions)
    }

    private fun loadModel(modelUri: ModelUri) = flow<Event> {
        try {
            var collectedModelsDeferred: Deferred<List<String>>? = null
            var modelDeferred: Deferred<ModelRenderable?>? = null
            viewModelScope.launch(dispatcher) {
                collectedModelsDeferred = async {
                    filesRepository.loadModelsFromCollection().map { it.absolutePath }
                }
                modelDeferred = async {
                    modelsRepository.loadModel(modelUri.uri)
                }
            }.join()
            val collectedModels = collectedModelsDeferred!!.await()
            val model = modelDeferred!!.await()
            if (model != null) {
                emit(
                    Event.ModelLoaded(
                        model = model,
                        addedToCollection = collectedModels.contains(modelUri.uri)
                    )
                )
            } else {
                postSideEffect(SideEffect.ErrorLoadingModel)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            postSideEffect(SideEffect.ErrorLoadingModel)
        }
    }

    private fun saveToCollection(modelUri: ModelUri) = flow<Event> {
        try {
            val savedName = filesRepository.saveModelToCollection(modelUri.toFileUri())
            postSideEffect(SideEffect.SavedToCollection(savedName))
        } catch (e: Throwable) {
            e.printStackTrace()
            postSideEffect(SideEffect.ErrorSaveToCollection)
        }
    }

    private fun removeFromCollection(modelUri: ModelUri.File) = flow<Event> {
        try {
            filesRepository.removeModelFromCollection(modelUri.toFileUri())
            postSideEffect(SideEffect.RemovedFromCollection(modelUri.uri.substringAfterLast("/")))
        } catch (e: Throwable) {
            e.printStackTrace()
            postSideEffect(SideEffect.ErrorRemoveFromCollection)
        }
    }

    private fun reduce(state: State, event: Event): State {
        return when(event) {
            is Event.ShowOptions -> state.copy(
                showOptions = true
            )
            is Event.ModelLoaded -> state.copy(
                isLoading = false,
                addedToCollection = event.addedToCollection,
                model = event.model
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
        addedToCollection = addedToCollection,
        model = model,
        showOptions = showOptions
    )
}