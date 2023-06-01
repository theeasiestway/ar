package com.theeasiestway.stereoar.ui.screens.model_view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ar.sceneform.rendering.ModelRenderable
import com.theeasiestway.domain.repositories.files.models.CollectedModel
import com.theeasiestway.domain.repositories.downloads.models.DownloadStatus
import com.theeasiestway.domain.repositories.downloads.DownloadsRepository
import com.theeasiestway.domain.repositories.files.FilesRepository
import com.theeasiestway.domain.repositories.models.ModelsRepository
import com.theeasiestway.stereoar.di.modelViewScopeId
import com.theeasiestway.stereoar.ui.screens.common.compose.permissions.PermissionResult
import com.theeasiestway.stereoar.ui.screens.common.ext.postSideEffect
import com.theeasiestway.stereoar.ui.screens.common.ext.state
import com.theeasiestway.stereoar.ui.screens.common.koin.closeScope
import com.theeasiestway.stereoar.ui.screens.models_explorer.FileUri
import com.theeasiestway.stereoar.ui.screens.models_explorer.ModelUri
import com.theeasiestway.stereoar.ui.screens.models_explorer.toDomain
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.koin.core.component.KoinComponent
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container

class ModelViewViewModel(
    private val modelsRepository: ModelsRepository<ModelRenderable>,
    private val filesRepository: FilesRepository,
    private val downloadsRepository: DownloadsRepository,
    private val appScope: CoroutineScope,
    private val dispatcherIO: CoroutineDispatcher,
    private val dispatcherMain: CoroutineDispatcher
): ContainerHost<State, ModelViewViewModel.SideEffect>, ViewModel(), KoinComponent {

    override val container = container<State, SideEffect>(State()) {
        handleIntent(Intent.RequestPermissions)
    }
    val uiState = state.map { it.toUiState() }

    sealed interface Intent {
        object RequestPermissions: Intent
        data class LoadModel(val footPrintModelUri: FileUri, val modelUri: ModelUri, val loadingText: String): Intent
        object HandleSceneCleared: Intent
        data class HandlePermissionResult(val result: PermissionResult): Intent
        object HandleTopBarActionClick: Intent
        data class HandleOptionsClick(val option: ModelViewOptions?): Intent
        object HandleBackClick: Intent
    }

    sealed interface Event {
        object RequestPermissions: Event
        data class ShowOptions(val options: List<ModelViewOptions>): Event
        data class ModelLoading(val modelStatus: ModelLoadingStatus.Progress): Event
        data class ModelLoaded(
            val modelStatus: ModelLoadingStatus.Done,
            val addedToCollection: Boolean,
            val collectedModels: List<CollectedModel>
        ): Event
        object ClearScene: Event
        object SceneCleared: Event
        object SavedToCollection: Event
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
            is Intent.LoadModel -> loadModel(footPrintModelUri = intent.footPrintModelUri, modelUri = intent.modelUri, loadingText = intent.loadingText)
            is Intent.HandlePermissionResult -> handlePermissionResult(result = intent.result)
            is Intent.HandleSceneCleared -> handleSceneCleared()
            is Intent.HandleTopBarActionClick -> handleTopBarActionClick(modelStatus = state.modelStatus!!, addedToCollection = state.addedToCollection)
            is Intent.HandleOptionsClick -> handleOptionsClick(option = intent.option)
            is Intent.HandleBackClick -> handleBackClick(state.modelStatus)
        }
    }

    private fun handleBackClick(modelStatus: ModelLoadingStatus?): Flow<Event> {
        if (modelStatus is ModelLoadingStatus.Progress) {
            appScope.launch {
                downloadsRepository.cancelDownload(modelStatus.downloadId)
            }
        }
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

    private fun handleSceneCleared() = flow<Event> {
        emit(Event.SceneCleared)
    }

    private fun handleTopBarActionClick(modelStatus: ModelLoadingStatus, addedToCollection: Boolean) = flow<Event> {
        if (modelStatus is ModelLoadingStatus.Done) {
            emit(
                Event.ShowOptions(
                    mutableListOf(
                        ModelViewOptions.ClearScene,
                        ModelViewOptions.AppSettings
                    ).apply {
                        if (!addedToCollection) {
                            add(0, ModelViewOptions.SaveToCollection(modelStatus.modelUri))
                        }
                    }
                )
            )
        }
    }

    private fun handleOptionsClick(option: ModelViewOptions?) = flow {
        emit(Event.ShowOptions(options = emptyList()))
        when(option) {
            is ModelViewOptions.SaveToCollection -> {
                saveToCollection(modelUri = option.modelUri, flow = this)
            }
            is ModelViewOptions.ClearScene -> {
                emit(Event.ClearScene)
            }
            is ModelViewOptions.AppSettings -> {
                postSideEffect(SideEffect.OpenAppSettings)
            }
            null -> Unit
        }
    }

    private suspend fun saveToCollection(modelUri: FileUri, flow: FlowCollector<Event>) {
        try {
            val savedName = filesRepository.saveToCollection(
                file = modelUri.toDomain()
            )
            flow.emit(Event.SavedToCollection)
            postSideEffect(SideEffect.SavedToCollection(savedName.substringAfterLast("/")))
        } catch (e: Throwable) {
            e.printStackTrace()
            postSideEffect(SideEffect.ErrorSaveToCollection)
        }
    }

    private fun loadModel(
        footPrintModelUri: FileUri,
        modelUri: ModelUri,
        loadingText: String
    ) = channelFlow {
        viewModelScope.launch(dispatcherIO) {
            try {
                var modelFileUri: FileUri? = null
                coroutineScope {
                    val collectedModelsDeferred = async {
                        filesRepository.getCollectedModels()
                    }
                    val footPrintModelDeferred = async(dispatcherMain) {
                        modelsRepository.getModel(footPrintModelUri.uri)!!
                    }
                    val modelDeferred = async(dispatcherMain) {
                        if (modelUri is FileUri) {
                            modelFileUri = modelUri
                            modelsRepository.getModel(modelUri.uri)!!
                        } else {
                            val downloadedModelUri = downloadsRepository.downloadFile(
                                url = modelUri.uri,
                                folderToSave = filesRepository.getModelsCollectionPath(),
                                fileNameToSave = filesRepository.getNewModelNameToCollect(),
                                cancelPrevDownloadsForSaveFolder = true,
                                notificationTitle = loadingText
                            ).onEach { download ->
                                if (download.status is DownloadStatus.Progress) {
                                    val (downloadedBytes, totalBytes) = download.status as DownloadStatus.Progress
                                    send(
                                        Event.ModelLoading(
                                            modelStatus = ModelLoadingStatus.Progress(
                                                downloadId = download.id,
                                                downloadedBytes = downloadedBytes.bytes,
                                                totalBytes = totalBytes.bytes
                                            )
                                        )
                                    )
                                } else if (download.status is DownloadStatus.Error) {
                                    throw (download.status as DownloadStatus.Error).reason
                                }
                            }.mapNotNull { download ->
                                (download.status as? DownloadStatus.Success)?.fileUri
                            }.first()
                            modelFileUri = FileUri(downloadedModelUri)
                            modelsRepository.getModel(downloadedModelUri)!!
                        }
                    }
                    val collectedModels = collectedModelsDeferred.await()
                    val footPrintModel = footPrintModelDeferred.await()
                    val model = modelDeferred.await()
                    send(
                        Event.ModelLoaded(
                            modelStatus = ModelLoadingStatus.Done(
                                modelUri = modelFileUri!!,
                                model = model,
                                footPrintModel = footPrintModel
                            ),
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
            is Event.ModelLoading -> state.copy(
                requestPermissions = false,
                modelStatus = event.modelStatus
            )
            is Event.ModelLoaded -> state.copy(
                isLoading = false,
                requestPermissions = false,
                addedToCollection = event.addedToCollection,
                modelStatus = event.modelStatus,
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
        modelStatus = modelStatus,
        options = options,
        clearScene = clearScene
    )
}