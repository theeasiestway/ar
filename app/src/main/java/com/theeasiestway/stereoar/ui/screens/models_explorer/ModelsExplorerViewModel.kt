package com.theeasiestway.stereoar.ui.screens.models_explorer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.theeasiestway.domain.repositories.files.FilesRepository
import com.theeasiestway.domain.repositories.files.models.CollectedModel
import com.theeasiestway.domain.repositories.files.models.Dir
import com.theeasiestway.domain.repositories.files.models.FilesTree
import com.theeasiestway.domain.repositories.settings.SettingsRepository
import com.theeasiestway.stereoar.ui.screens.common.compose.permissions.PermissionResult
import com.theeasiestway.stereoar.ui.screens.common.ext.postSideEffect
import com.theeasiestway.stereoar.ui.screens.common.ext.state
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container

class ModelsExplorerViewModel(
    private val filesRepository: FilesRepository,
    private val settingsRepository: SettingsRepository,
    private val ioDispatcher: CoroutineDispatcher
): ContainerHost<State, ModelsExplorerViewModel.SideEffect>, ViewModel(), KoinComponent {

    override val container = container<State, SideEffect>(State()) {
        handleIntent(Intent.RequestPermissions)
    }
    val uiState = state.map { it.toUiState() }

    sealed interface Intent {
        object RequestPermissions: Intent
        data class HandlePermissionResult(val result: PermissionResult): Intent
        object HandleTopBarActionClick: Intent
        data class HandleOptionsClick(val option: ModelsExplorerOptions?): Intent
        object LoadData: Intent
        object UpdateCollectedModels: Intent
        data class OpenFile(val file: FileItem): Intent
        data class LoadModel(val url: String?): Intent
        data class ShowModel(val model: CollectedModel): Intent
        data class ShowModelOptions(val model: CollectedModel?): Intent
        data class HandleModelOptionsClick(val option: CollectedModelOptions?): Intent
        data class RenameModel(val newName: String, val model: CollectedModel): Intent
        data class DeleteModel(val model: CollectedModel?): Intent
        data class GoBack(val fromFilesExplorer: Boolean): Intent
    }

    sealed interface Event {
        object RequestPermissions: Event
        data class DataLoading(val isLoading: Boolean): Event
        data class DataLoaded(
            val files: FilesTree,
            val collectedModels: List<CollectedModel>
        ): Event
        data class FolderOpened(val filesTree: FilesTree): Event
        data class ShowOptions(val show: Boolean): Event
        data class ShowModelOptions(val model: CollectedModel?): Event
        data class DownloadModel(val show: Boolean): Event
        data class ModelOptionSelected(val option: CollectedModelOptions?): Event
        data class CollectedModelsChanged(val collectedModels: List<CollectedModel>): Event
    }

    sealed interface SideEffect {
        data class OpenModelScreen(val modelUri: ModelUri): SideEffect
        object OpenAppSettings: SideEffect
        object CloseApp: SideEffect
        object ErrorLoadingData: SideEffect
        data class ErrorOpeningFile(val isFolder: Boolean): SideEffect
        object ErrorRenameModel: SideEffect
        object ErrorDeleteModel: SideEffect
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
            is Intent.HandlePermissionResult -> handlePermissionResult(result = intent.result)
            is Intent.HandleTopBarActionClick -> handleTopBarActionClick()
            is Intent.HandleOptionsClick -> handleOptionsClick(option = intent.option)
            is Intent.LoadData -> loadData()
            is Intent.UpdateCollectedModels -> updateCollectedModels()
            is Intent.OpenFile -> openFile(file = intent.file)
            is Intent.LoadModel -> downloadModel(url = intent.url)
            is Intent.ShowModel -> showModel(uri = intent.model.path, isFile = true)
            is Intent.ShowModelOptions -> showModelOptions(model = intent.model)
            is Intent.HandleModelOptionsClick -> handleModelOptionsClick(option = intent.option)
            is Intent.RenameModel -> renameModel(newName = intent.newName, model = intent.model)
            is Intent.DeleteModel -> deleteModel(model = intent.model)
            is Intent.GoBack -> goBack(files = state.files, fromFilesExplorer = intent.fromFilesExplorer)
        }
    }

    private fun requestPermissions() = flow<Event> {
        emit(Event.RequestPermissions)
    }

    private fun handlePermissionResult(result: PermissionResult): Flow<Event> {
        return when(result) {
            PermissionResult.Granted -> loadData()
            PermissionResult.DeniedForeverAndCanceled -> {
                postSideEffect(SideEffect.CloseApp)
                emptyFlow()
            }
        }
    }

    private fun handleTopBarActionClick() = flow<Event> {
        emit(Event.ShowOptions(show = true))
    }

    private fun handleOptionsClick(option: ModelsExplorerOptions?) = flow {
        when(option) {
            ModelsExplorerOptions.DownloadModel -> emit(Event.DownloadModel(show = true))
            ModelsExplorerOptions.AppSettings -> postSideEffect(SideEffect.OpenAppSettings)
            null -> emit(Event.ShowOptions(show = false))
        }
    }

    private fun loadData() = channelFlow<Event> {
        viewModelScope.launch(ioDispatcher) {
            try {
                coroutineScope {
                    val filesTreeDeferred = async {
                        val lastFolder = null //settingsRepository.loadLastVisitedFolder() TODO
                        filesRepository.getLastVisitedFolder(lastFolder)
                    }
                    val modelsCollectionDeferred = async {
                        filesRepository.getCollectedModels()
                    }
                    val filesTree = filesTreeDeferred.await()
                    val collectedModels = modelsCollectionDeferred.await()
                    send(
                        Event.DataLoaded(
                            files = filesTree,
                            collectedModels = collectedModels
                        )
                    )
                }
            } catch(e: Throwable) {
                e.printStackTrace()
                postSideEffect(SideEffect.ErrorLoadingData)
            }
        }.join()
    }

    private fun updateCollectedModels() = flow<Event> {
        try {
            val collectedModels = filesRepository.getCollectedModels()
            emit(Event.CollectedModelsChanged(collectedModels))
        } catch(e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun openFile(file: FileItem): Flow<Event> {
        return openFile(
            path = file.path,
            isFolder = file is FileItem.Root || file is FileItem.NotRoot.Folder
        )
    }

    private fun openFile(path: String, isFolder: Boolean): Flow<Event> {
        return try {
            if (isFolder) {
                flow {
                    val files = filesRepository.openFolder(path)
                    emit(Event.FolderOpened(files))
                }
            } else {
                showModel(
                    uri = path,
                    isFile = true
                )
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            postSideEffect(SideEffect.ErrorOpeningFile(isFolder))
            emptyFlow()
        }
    }

    private fun downloadModel(url: String?): Flow<Event> {
        return if (url != null) {
            showModel(uri = url, isFile = false)
        } else flow {
            emit(Event.DownloadModel(show = false))
        }
    }

    private fun showModel(uri: String, isFile: Boolean) = flow<Event> {
        val modelUri = if (isFile) FileUri(uri = uri)
        else UrlUri(uri = uri)
        postSideEffect(SideEffect.OpenModelScreen(modelUri))
        emit(Event.DownloadModel(show = false))
    }

    private fun showModelOptions(model: CollectedModel?) = flow<Event> {
        emit(Event.ShowModelOptions(model = model))
    }

    private fun handleModelOptionsClick(option: CollectedModelOptions?) = flow {
        emit(Event.ModelOptionSelected(option = option))
    }

    private fun renameModel(newName: String, model: CollectedModel) = flow {
        try {
            emit(Event.ModelOptionSelected(option = null))
            filesRepository.renameCollectedModel(newName, model)
            val collectedModels = filesRepository.getCollectedModels()
            emit(Event.CollectedModelsChanged(collectedModels = collectedModels))
        } catch (e: Throwable) {
            e.printStackTrace()
            postSideEffect(SideEffect.ErrorRenameModel)
        }
    }

    private fun deleteModel(model: CollectedModel?) = flow {
        try {
            emit(Event.ModelOptionSelected(option = null))
            if (model != null) {
                filesRepository.deleteCollectedModel(model)
                val collectedModels = filesRepository.getCollectedModels()
                emit(Event.CollectedModelsChanged(collectedModels = collectedModels))
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            postSideEffect(SideEffect.ErrorDeleteModel)
        }
    }

    private fun goBack(files: FilesTree, fromFilesExplorer: Boolean): Flow<Event> {
        return if (fromFilesExplorer) {
            files.parentPath?.let { path ->
                openFile(path, true)
            } ?: emptyFlow()
        } else {
            postSideEffect(SideEffect.CloseApp)
            emptyFlow()
        }
    }

    private fun reduce(state: State, event: Event): State {
        return when(event) {
            is Event.RequestPermissions -> state.copy(
                requestPermissions = true
            )
            is Event.DataLoaded -> state.copy(
                isLoading = false,
                requestPermissions = false,
                files = event.files,
                collectedModels = event.collectedModels
            )
            is Event.DataLoading -> state.copy(
                isLoading = event.isLoading
            )
            is Event.CollectedModelsChanged -> state.copy(
                collectedModels = event.collectedModels
            )
            is Event.FolderOpened -> state.copy(
                isLoading = false,
                files = event.filesTree
            )
            is Event.ShowOptions -> state.copy(
                showOptions = event.show
            )
            is Event.ShowModelOptions -> state.copy(
                selectedCollectedModel = event.model
            )
            is Event.DownloadModel -> state.copy(
                showDownloadModel = event.show,
                showOptions = false
            )
            is Event.ModelOptionSelected -> state.copy(
                selectedCollectedModel = null,
                selectedCollectedModelOption = event.option
            )
        }
    }
}

private fun State.toUiState(): UiState {
    return UiState(
        isLoading = isLoading,
        requestPermissions = requestPermissions,
        pages = toPages(),
        showOptions = showOptions,
        showDownloadModel = showDownloadModel,
        selectedCollectedModelOption = selectedCollectedModelOption
    )
}

private fun State.toPages(): List<PagerPage> {
    return listOf(
        PagerPage.FilesExplorer(
            displayablePath = files.toDisplayablePath(),
            currentPath = files.currentPath,
            files = files.toUi(),
            canMoveBack = files.parentPath != null
        ),
        PagerPage.ModelsCollection(
            models = collectedModels,
            selectedModel = selectedCollectedModel
        )
    )
}

private fun FilesTree.toDisplayablePath(): DisplayablePath {
    val isRoot = currentPath == rootPath
    val isExternalStorage = externalStorageRootPath != null && currentPath.startsWith(externalStorageRootPath!!, true)
    return when {
        isRoot -> DisplayablePath.Root
        isExternalStorage -> {
            DisplayablePath.ExternalStorage(
                path = currentPath.split(externalStorageRootPath!!, ignoreCase = true)[1] // todo move this logic to FileRepository
            )
        }
        else -> DisplayablePath.InternalStorage(
            path = currentPath.split(internalStorageRootPath, ignoreCase = true)[1]
        )
    }
}

private fun FilesTree.toUi(): List<FileItem> {
    return files.map { dir ->
        dir.toUi(
            isRoot = currentPath == rootPath,
            externalStoragePath = externalStorageRootPath
        )
    }
}

private fun Dir.toUi(
    isRoot: Boolean,
    externalStoragePath: String?
): FileItem {
    return if (isRoot) {
        val isExternalStorage = externalStoragePath != null && absolutePath.startsWith(externalStoragePath)
        FileItem.Root(
            parentPath = parentPath,
            path = absolutePath,
            isExternalStorage = isExternalStorage
        )
    } else if (this is Dir.Folder) {
        FileItem.NotRoot.Folder(
            parentPath = parentPath,
            path = absolutePath,
            title = name,
            creationDateMillis = creationDateMillis,
            filesCount = filesCount
        )
    } else {
        FileItem.NotRoot.File(
            parentPath = parentPath,
            path = absolutePath,
            title = name,
            creationDateMillis = creationDateMillis,
            sizeBytes = sizeBytes
        )
    }
}