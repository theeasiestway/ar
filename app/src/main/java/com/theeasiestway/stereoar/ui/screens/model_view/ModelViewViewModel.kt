package com.theeasiestway.stereoar.ui.screens.model_view

import androidx.lifecycle.ViewModel
import com.google.ar.sceneform.rendering.ModelRenderable
import com.theeasiestway.domain.repositories.ModelsRepository
import com.theeasiestway.stereoar.di.modelViewScopeId
import com.theeasiestway.stereoar.ui.screens.common.koin.closeScope
import com.theeasiestway.stereoar.ui.screens.common.state
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container

class ModelViewViewModel(
    private val modelsRepository: ModelsRepository<ModelRenderable>
): ContainerHost<State, ModelViewViewModel.SideEffect>, ViewModel(), KoinComponent {

    override val container = container<State, SideEffect>(State())
    val uiState = state.map { it.toUiState() }

    sealed interface Intent {
        data class LoadModel(val modelUri: String): Intent
        data class SaveToCollection(val modelUri: String): Intent
        data class RemoveFromCollection(val modelUri: String): Intent
    }

    sealed interface Event {
        data class ModelLoaded(
            val model: ModelRenderable,
            val addedToCollection: Boolean
        ): Event
        data class SavedToCollection(val modelUri: String): Event
        data class RemovedFromCollection(val modelUri: String): Event
    }

    sealed interface SideEffect {
        object SavedToCollection: SideEffect
        object RemovedFromCollection: SideEffect
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
            is Intent.LoadModel -> loadModel(intent.modelUri)
            is Intent.SaveToCollection -> saveToCollection(intent.modelUri)
            is Intent.RemoveFromCollection -> removeFromCollection(intent.modelUri)
        }
    }

    private fun loadModel(modelUri: String) = flow<Event> {

    }

    private fun saveToCollection(modelUri: String) = flow<Event> {

    }

    private fun removeFromCollection(modelUri: String) = flow<Event> {

    }

    private fun reduce(state: State, event: Event): State {
        return when(event) {
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
        model = model
    )
}