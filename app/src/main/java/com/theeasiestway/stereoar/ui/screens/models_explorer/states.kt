package com.theeasiestway.stereoar.ui.screens.models_explorer

import com.theeasiestway.domain.model.FilesTree
import java.io.File

data class State(
    val isLoading: Boolean = false,
    val permissionsGranted: Boolean = false,
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
    val showCloseAppDialog: Boolean = false
)

data class UiState(
    val permissionsGranted: Boolean = false,
    val pages: List<PagerPage> = emptyList(),
    val showOptions: Boolean = false,
    val showCloseAppDialog: Boolean = false
)