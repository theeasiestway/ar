package com.theeasiestway.stereoar.ui.screens.models_explorer

import com.theeasiestway.domain.repositories.files.models.CollectedModel
import com.theeasiestway.domain.repositories.files.models.FilesTree

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
    val collectedModels: List<CollectedModel> = emptyList(),
    val showOptions: Boolean = false,
    val showDownloadModel: Boolean = false,
    val selectedCollectedModel: CollectedModel? = null,
    val selectedCollectedModelOption: CollectedModelOptions? = null
)

data class UiState(
    val isLoading: Boolean = true,
    val requestPermissions: Boolean = false,
    val pages: List<PagerPage> = emptyList(),
    val showOptions: Boolean = false,
    val showDownloadModel: Boolean = false,
    val selectedCollectedModelOption: CollectedModelOptions? = null
)