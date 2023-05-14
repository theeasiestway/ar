package com.theeasiestway.stereoar.ui.screens.models_explorer

import com.theeasiestway.domain.model.FilesTree
import java.io.File

data class State(
    val isLoading: Boolean = true,
    val requestPermissions: Boolean = false,
    val files: FilesTree = FilesTree(
        rootPath = "",
        internalStorageRootPath = "",
        externalStorageRootPath = "",
        currentPath = "",
        parentPath = "",
        files = emptyList()
    ),
    val collectedModels: List<File> = emptyList(),
    val showOptions: Boolean = false,
    val showDownloadModel: Boolean = false
)

data class UiState(
    val isLoading: Boolean = true,
    val requestPermissions: Boolean = false,
    val pages: List<PagerPage> = emptyList(),
    val showOptions: Boolean = false,
    val showDownloadModel: Boolean = false
)