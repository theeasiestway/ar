package com.theeasiestway.stereoar.ui.screens.choose_model.viewmodel

import android.app.Application
import android.content.Context
import android.os.Environment
import android.text.format.DateFormat
import android.text.format.Formatter.formatFileSize
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.theeasiestway.stereoar.R
import com.theeasiestway.stereoar.ui.screens.choose_model.FileItem
import com.theeasiestway.stereoar.ui.screens.choose_model.ModelItem
import com.theeasiestway.stereoar.ui.screens.choose_model.PagerPage
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Alexey Loboda on 17.07.2022
 */
class ChooseArModelViewModelImpl(application: Application): AndroidViewModel(application),
    ChooseArModelViewModel {

    private val tag = javaClass.simpleName
    private val datePattern12 = "dd.MM.yyyy h:mm a"
    private val datePattern24 = "dd.MM.yyyy HH:mm"
    private val dateFormat = SimpleDateFormat(getDatePattern(), Locale.getDefault())
    private val defaultFolderPath = "/${UUID.randomUUID()}"
    private val internalStoragePath = Environment.getExternalStorageDirectory().absolutePath
    private val externalStoragePath = getExternalStoragePath()
    private val _arModel = MutableSharedFlow<ModelItem>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val _pages = MutableStateFlow(
        listOf(
            PagerPage.FilesExplorerState(
                title = R.string.models_explorer_title,
                displayablePath = getDisplayablePath(defaultFolderPath),
                currentPath = defaultFolderPath,
                files = loadFilesList(File(defaultFolderPath)),
                canMoveBack = false
            ),
            PagerPage.FavoritesModelsState(
                title = R.string.choose_ar_model_favorite_models,
                models = emptyList()
            )
        )
    )
    private val pages = _pages.asStateFlow()
    private val arModel = _arModel.asSharedFlow()

    init {
        Log.d("qwdqwdwq", "getExternalFilesDirs: ${requireContext().getExternalFilesDirs(null).toList()}")
        Log.d("qwdqwdwq", "defaultFolderPath: $defaultFolderPath")
        Log.d("qwdqwdwq", "internalStoragePath: $internalStoragePath")
        Log.d("qwdqwdwq", "externalStoragePath: $externalStoragePath")
    }

    override fun setModelPath(path: String?) {
        if (path != null) openFolder(path)
    }

    override fun onFilesPermissionsGranted() {
        val currentPath = pages.value
            .filterIsInstance<PagerPage.FilesExplorerState>()
            .first()
            .currentPath
        openFolder(currentPath)
    }

    override fun onFileClicked(item: FileItem) {
        when(item) {
            is FileItem.File -> {
                chooseArModel(
                    ModelItem(
                        item.path,
                        item.title,
                        item.size,
                        item.creationDate
                    )
                )
            }
            is FileItem.Folder -> {
                openFolder(item.path)
            }
        }
    }

    override fun onBackClicked(path: String) {
        val parentPath = when(path) {
            internalStoragePath,
            externalStoragePath,
            defaultFolderPath -> defaultFolderPath
            else -> File(path).parentFile?.absolutePath
        }
        if (parentPath != null) openFolder(parentPath)
    }

    override fun onModelClicked(item: ModelItem) {
        chooseArModel(item)
    }

    private fun chooseArModel(model: ModelItem) {
        _arModel.tryEmit(model)
    }

    private fun openFolder(path: String) {
        val file = File(path)
        _pages.update { pages ->
            pages.map { page ->
                when(page) {
                    is PagerPage.FilesExplorerState -> {
                        page.copy(
                            displayablePath = getDisplayablePath(file.absolutePath),
                            currentPath = file.absolutePath,
                            files = loadFilesList(file),
                            canMoveBack = file.parentFile != null && file.absolutePath != defaultFolderPath
                        )
                    }
                    else -> page
                }
            }
        }
    }

    private fun createStoragesList(): List<FileItem> {
        val storages = mutableListOf(
            FileItem.Folder(
                parentPath = null,
                path = internalStoragePath,
                title = getInternalStorageLabel(),
                icon = R.drawable.ic_storage,
                creationDate = null,
                itemsCount = null
            )
        )
        if (externalStoragePath != null) {
            storages.add(
                FileItem.Folder(
                    parentPath = null,
                    path = externalStoragePath,
                    title = getExternalStorageLabel(),
                    icon = R.drawable.ic_sd_card,
                    creationDate = null,
                    itemsCount = null
                )
            )
        }
        return storages
    }

    private fun loadFilesList(file: File): List<FileItem> {
        if (file.absolutePath == defaultFolderPath) {
            return createStoragesList()
        }
        dateFormat.applyPattern(getDatePattern())
        return file.listFiles()?.mapNotNull { childFile ->
            if (childFile.isDirectory) {
                FileItem.Folder(
                    parentPath = childFile.parentFile?.absolutePath,
                    path = childFile.absolutePath,
                    title = childFile.name,
                    icon = R.drawable.ic_folder,
                    creationDate = dateFormat.format(childFile.lastModified()),
                    itemsCount = formatFilesCount(childFile.listFiles()?.size ?: 0)
                )
            } else if (childFile.isFile) {
                FileItem.File(
                    parentPath = childFile.parentFile?.absolutePath,
                    path = childFile.absolutePath,
                    title = childFile.name,
                    icon = R.drawable.ic_file,
                    creationDate = dateFormat.format(childFile.lastModified()),
                    size = formatFileSize(requireContext(), childFile.length())
                )
            } else null
        } ?: emptyList()
    }

    private fun formatFilesCount(count: Int): String {
        return "$count ${requireContext().resources.getQuantityString(R.plurals.general_files_count, count).lowercase(Locale.getDefault())}"
    }

    override fun observePages(): Flow<List<PagerPage>> {
        return pages
    }

    override fun observeArModel(): Flow<ModelItem> {
        return arModel
    }

    private fun getDisplayablePath(path: String): String {
        return when {
            path == defaultFolderPath -> {
                requireContext().getString(R.string.models_explorer_storage)
            }
            path.startsWith(internalStoragePath, true) -> {
                "${getInternalStorageLabel()}${path.split(internalStoragePath, ignoreCase = true)[1]}"
            }
            externalStoragePath != null && path.startsWith(externalStoragePath, true) -> {
                "${getExternalStorageLabel()}${path.split(externalStoragePath, ignoreCase = true)[1]}"
            }
            else -> path
        }
    }

    private fun getDatePattern(): String {
        return if (DateFormat.is24HourFormat(requireContext())) datePattern24 else datePattern12
    }

    private fun getExternalStoragePath(): String? {
        val splitString = "/Android/data"
        return if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED ||
            Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED_READ_ONLY) {
            requireContext().getExternalFilesDirs(null).firstOrNull { dir ->
                val startPath = dir.absolutePath
                    .split(splitString, ignoreCase = true)
                    .firstOrNull() ?: ""
                !startPath.contains(internalStoragePath, true)
            }?.absolutePath?.split(splitString, ignoreCase = true)?.firstOrNull()
        } else null
    }

    private fun getInternalStorageLabel(): String {
        return requireContext().getString(R.string.models_explorer_internal_storage)
    }

    private fun getExternalStorageLabel(): String {
        return requireContext().getString(R.string.models_explorer_sd_card)
    }

    private fun requireContext(): Context {
        return getApplication<Application>().applicationContext
    }
}