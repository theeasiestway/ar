package com.theeasiestway.stereoar.ui.screens.models_explorer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.theeasiestway.domain.model.CollectedModel
import com.theeasiestway.domain.model.FilesTree
import com.theeasiestway.domain.repositories.FilesRepository
import com.theeasiestway.domain.repositories.SettingsRepository
import com.theeasiestway.stereoar.di.modelsExplorerScopeId
import com.theeasiestway.stereoar.ui.screens.common.koin.closeScope
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
    private val dispatcherIO: CoroutineDispatcher
): ContainerHost<State, ModelsExplorerViewModel.SideEffect>, ViewModel(), KoinComponent {

    override val container = container<State, SideEffect>(State()) {
        handleIntent(Intent.CheckPermissions(false))
    }
    val uiState = state.map { it.toUiState() }

    sealed interface Intent {
        data class CheckPermissions(val isGranted: Boolean): Intent
        object LoadData: Intent
        object GoBack: Intent
        data class OpenFile(val file: FileItem): Intent
        object ShowOptions: Intent
        data class ShowModel(val model: CollectedModel): Intent
    }

    sealed interface Event {
        data class DataLoading(val isLoading: Boolean): Event
        data class DataLoaded(
            val files: FilesTree,
            val collectedModels: List<File>
        ): Event
        data class FolderOpened(val filesTree: FilesTree): Event
        object ShowOptions: Event
        object ShowCloseAppDialog: Event
    }

    sealed interface SideEffect {
        object RequestPermissions: SideEffect
        data class OpenShowModelScreen(val modelUri: String): SideEffect
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
            is Intent.CheckPermissions -> checkPermissions(intent.isGranted)
            is Intent.LoadData -> loadData()
            is Intent.GoBack -> goBack(state)
            is Intent.OpenFile -> openFile(
                path = intent.file.path,
                isFolder = intent.file is FileItem.Root || intent.file is FileItem.NotRoot.Folder
            )
            is Intent.ShowModel -> showModel(model = intent.model)
            is Intent.ShowOptions -> showOptions()
        }
    }

    private fun checkPermissions(isGranted: Boolean): Flow<Event> {
        return if (isGranted) loadData()
        else {
            postSideEffect(SideEffect.RequestPermissions)
            emptyFlow()
        }
    }

    private fun goBack(state: State): Flow<Event> {
        return state.files.parentPath?.let { path ->
            openFile(path, true)
        } ?: run {
            flow {
                emit(Event.ShowCloseAppDialog)
            }
        }
    }

    private fun showOptions() = flow<Event> {
        emit(Event.ShowOptions)
    }

    private fun loadData() = flow<Event> {
        try {
            var filesTreeDeferred: Deferred<FilesTree>? = null
            var modelsCollectionDeferred: Deferred<List<File>>? = null

            viewModelScope.launch(dispatcherIO) {
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

    private fun openFile(path: String, isFolder: Boolean) = flow<Event> {
        try {
            val files = filesRepository.openFolder(path)
            emit(Event.FolderOpened(files))
        } catch (e: Throwable) {
            e.printStackTrace()
            postSideEffect(SideEffect.ErrorOpeningFile(isFolder))
        }
    }

    private fun showModel(model: CollectedModel): Flow<Event> {
        return flow {
            postSideEffect(SideEffect.OpenShowModelScreen(model.path))
        }
    }

    private fun reduce(state: State, event: Event): State {
        return when(event) {
            is Event.DataLoaded -> state.copy(
                isLoading = false,
                permissionsGranted = true,
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
            is Event.ShowCloseAppDialog -> state.copy(
                showCloseAppDialog = true
            )
        }
    }

    override fun onCleared() {
        closeScope(modelsExplorerScopeId)
    }
}

private fun State.toUiState(): UiState {
    return UiState(
        permissionsGranted = permissionsGranted,
        pages = toPages(),
        showOptions = showOptions,
        showCloseAppDialog = showCloseAppDialog
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
                path = currentPath.split(externalStorageRootPath!!, ignoreCase = true)[1]
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