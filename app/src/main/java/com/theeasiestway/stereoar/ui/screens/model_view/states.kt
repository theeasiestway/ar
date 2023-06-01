package com.theeasiestway.stereoar.ui.screens.model_view

import com.theeasiestway.domain.repositories.files.models.CollectedModel

data class State(
    val isLoading: Boolean = true,
    val requestPermissions: Boolean = false,
    val addedToCollection: Boolean = false,
    val modelStatus: ModelLoadingStatus? = null,
    val collectedModels: List<CollectedModel> = emptyList(),
    val options: List<ModelViewOptions> = emptyList(),
    val clearScene: Boolean = false
)

data class UiState(
    val isLoading: Boolean = true,
    val requestPermissions: Boolean = false,
    val modelStatus: ModelLoadingStatus? = null,
    val options: List<ModelViewOptions> = emptyList(),
    val clearScene: Boolean = false
)