package com.theeasiestway.stereoar.ui.screens.models_explorer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.theeasiestway.domain.model.CollectedModel
import com.theeasiestway.domain.model.FilesTree
import com.theeasiestway.domain.repositories.FilesRepository
import com.theeasiestway.domain.repositories.SettingsRepository
import com.theeasiestway.stereoar.ui.screens.common.compose.permissions.PermissionResult
import com.theeasiestway.stereoar.ui.screens.common.compose.scaffold.TopBarAction
import com.theeasiestway.stereoar.ui.screens.common.postSideEffect
import com.theeasiestway.stereoar.ui.screens.common.state
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
import java.io.File

class ModelsExplorerViewModel(
    private val filesRepository: FilesRepository,
    private val settingsRepository: SettingsRepository,
    private val dispatcher: CoroutineDispatcher
): ContainerHost<State, ModelsExplorerViewModel.SideEffect>, ViewModel(), KoinComponent {

    override val container = container<State, SideEffect>(State()) {
        handleIntent(Intent.RequestPermissions)
    }
    val uiState = state.map { it.toUiState() }

    sealed interface Intent {
        object RequestPermissions: Intent
        data class HandlePermissionResult(val result: PermissionResult): Intent
        data class HandleTopBarActionClick(val action: TopBarAction): Intent
        object LoadData: Intent
        data class OpenFile(val file: FileItem): Intent
        object ShowOptions: Intent
        object GoBack: Intent
        data class LoadModel(val url: String): Intent
        data class ShowModel(val model: CollectedModel): Intent
    }

    sealed interface Event {
        object RequestPermissions: Event
        data class DataLoading(val isLoading: Boolean): Event
        data class DataLoaded(
            val files: FilesTree,
            val collectedModels: List<File>
        ): Event
        data class FolderOpened(val filesTree: FilesTree): Event
        object ShowOptions: Event
    }

    sealed interface SideEffect {
        object CloseApp: SideEffect
        data class OpenModelScreen(val modelUri: ModelUri): SideEffect
        object ErrorLoadingData: SideEffect
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
            is Intent.HandleTopBarActionClick -> handleTopBarActionClick(action = intent.action)
            is Intent.LoadData -> loadData()
            is Intent.OpenFile -> openFile(file = intent.file)
            is Intent.LoadModel -> showModel(uri = intent.url, isFile = false)
            is Intent.ShowModel -> showModel(uri = intent.model.path, isFile = true)
            is Intent.ShowOptions -> showOptions()
            is Intent.GoBack -> goBack(files = state.files)
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

    private fun handleTopBarActionClick(action: TopBarAction): Flow<Event> {
        return if (action == TopBarAction.More) showOptions()
        else emptyFlow()
    }

    private fun showOptions() = flow<Event> {
        emit(Event.ShowOptions)
    }

    private fun goBack(files: FilesTree): Flow<Event> {
        return files.parentPath?.let { path ->
            openFile(path, true)
        } ?: emptyFlow()
    }

    private fun loadData() = flow<Event> {
        try {
            var filesTreeDeferred: Deferred<FilesTree>? = null
            var modelsCollectionDeferred: Deferred<List<File>>? = null

            viewModelScope.launch(dispatcher) {
                filesTreeDeferred = async {
                    val lastFolder = settingsRepository.loadLastVisitedFolder()
                    filesRepository.loadLastVisitedFolder(lastFolder)
                }
                modelsCollectionDeferred = async {
                    filesRepository.loadModelsFromCollection()
                }
            }.join()

            val filesTree = filesTreeDeferred!!.await()
            val collectedModels = modelsCollectionDeferred!!.await()

            emit(
                Event.DataLoaded(
                    files = filesTree,
                    collectedModels = collectedModels
                )
            )
        } catch(e: Throwable) {
            e.printStackTrace()
            postSideEffect(SideEffect.ErrorLoadingData)
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

    private fun showModel(uri: String, isFile: Boolean) = flow<Event> {
        val modelUri = if (isFile) ModelUri.File(uri)
        else ModelUri.Url(uri)
        postSideEffect(SideEffect.OpenModelScreen(modelUri))
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
                showOptions = true
            )
        }
    }
}

private fun State.toUiState(): UiState {
    return UiState(
        isLoading = isLoading,
        requestPermissions = requestPermissions,
        pages = toPages(),
        showOptions = showOptions
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