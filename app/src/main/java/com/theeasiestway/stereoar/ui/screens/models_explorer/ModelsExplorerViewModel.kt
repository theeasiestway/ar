package com.theeasiestway.stereoar.ui.screens.models_explorer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.theeasiestway.domain.model.CollectedModel
import com.theeasiestway.domain.model.FilesTree
import com.theeasiestway.domain.repositories.FilesRepository
import com.theeasiestway.domain.repositories.SettingsRepository
import com.theeasiestway.stereoar.ui.screens.common.compose.permissions.PermissionResult
import com.theeasiestway.stereoar.ui.screens.common.ext.postSideEffect
import com.theeasiestway.stereoar.ui.screens.common.ext.state
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
import java.io.File

class ModelsExplorerViewModel(
    private val filesRepository: FilesRepository,
    private val settingsRepository: SettingsRepository,
    private val dispatcherIO: CoroutineDispatcher
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
        data class OpenFile(val file: FileItem): Intent
        data class LoadModel(val url: String?): Intent
        data class ShowModel(val model: CollectedModel): Intent
        object GoBack: Intent
    }

    sealed interface Event {
        object RequestPermissions: Event
        data class DataLoading(val isLoading: Boolean): Event
        data class DataLoaded(
            val files: FilesTree,
            val collectedModels: List<File>
        ): Event
        data class FolderOpened(val filesTree: FilesTree): Event
        data class ShowOptions(val show: Boolean): Event
        data class DownloadModel(val show: Boolean): Event
    }

    sealed interface SideEffect {
        object CloseApp: SideEffect
        data class OpenModelScreen(val modelUri: ModelUri): SideEffect
        object ErrorLoadingData: SideEffect
        object OpenAppSettings: SideEffect
        data class ErrorOpeningFile(val isFolder: Boolean): SideEffect
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
            is Intent.OpenFile -> openFile(file = intent.file)
            is Intent.LoadModel -> downloadModel(url = intent.url)
            is Intent.ShowModel -> showModel(uri = intent.model.path, isFile = true)
            is Intent.GoBack -> goBack(files = state.files)
        }
    }

    private fun handleOptionsClick(option: ModelsExplorerOptions?) = flow {
        when(option) {
            ModelsExplorerOptions.DownloadModel -> emit(Event.DownloadModel(show = true))
            ModelsExplorerOptions.AppSettings -> postSideEffect(SideEffect.OpenAppSettings)
            null -> emit(Event.ShowOptions(show = false))
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

    private fun goBack(files: FilesTree): Flow<Event> {
        return files.parentPath?.let { path ->
            openFile(path, true)
        } ?: emptyFlow()
    }

    private fun loadData() = channelFlow<Event> {
        viewModelScope.launch(dispatcherIO) {
            try {
                coroutineScope {
                    val filesTreeDeferred = async {
                        val lastFolder = settingsRepository.loadLastVisitedFolder()
                        filesRepository.loadLastVisitedFolder(lastFolder)
                    }
                    val modelsCollectionDeferred = async {
                        filesRepository.loadModelsFromCollection()
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
            showModel(url, false)
        } else flow {
            emit(Event.DownloadModel(show = false))
        }
    }

    private fun showModel(uri: String, isFile: Boolean) = flow<Event> {
        val modelUri = if (isFile) ModelUri.File(uri)
        else ModelUri.Url(uri)
        postSideEffect(SideEffect.OpenModelScreen(modelUri))
        emit(Event.DownloadModel(show = false))
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
            is Event.FolderOpened -> state.copy(
                isLoading = false,
                files = event.filesTree
            )
            is Event.ShowOptions -> state.copy(
                showOptions = event.show
            )
            is Event.DownloadModel -> state.copy(
                showDownloadModel = event.show,
                showOptions = false
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
        showDownloadModel = showDownloadModel
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
            models = collectedModels.toCollectedModels()
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
    return files.map { file ->
        file.toUi(
            isRoot = currentPath == rootPath,
            externalStoragePath = externalStorageRootPath
        )
    }
}

private fun File.toUi(
    isRoot: Boolean,
    externalStoragePath: String?
): FileItem {
    return if (isRoot) {
        val isExternalStorage = externalStoragePath != null && absolutePath.startsWith(externalStoragePath)
        FileItem.Root(
            parentPath = parent,
            path = absolutePath,
            isExternalStorage = isExternalStorage
        )
    } else if (isDirectory) {
        FileItem.NotRoot.Folder(
            parentPath = parent,
            path = absolutePath,
            title = name,
            creationDateMillis = lastModified(),
            filesCount = listFiles()?.size ?: 0
        )
    } else {
        FileItem.NotRoot.File(
            parentPath = parent,
            path = absolutePath,
            title = name,
            creationDateMillis = lastModified(),
            sizeBytes = length()
        )
    }
}

private fun List<File>.toCollectedModels(): List<CollectedModel> {
    return map { file ->
        CollectedModel(
            name = file.name,
            path = file.absolutePath,
            sizeBytes = file.length(),
            creationDateMillis = file.lastModified()
        )
    }
}