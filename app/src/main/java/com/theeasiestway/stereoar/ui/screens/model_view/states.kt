package com.theeasiestway.stereoar.ui.screens.model_view

import com.google.ar.sceneform.rendering.ModelRenderable
import com.theeasiestway.domain.model.CollectedModel
import com.theeasiestway.stereoar.ui.screens.models_explorer.ModelUri

data class State(
    val isLoading: Boolean = true,
    val requestPermissions: Boolean = false,
    val addedToCollection: Boolean = false,
    val modelUri: ModelUri.File? = null,
    val footPrintModel: ModelRenderable? = null,
    val model: ModelRenderable? = null,
    val collectedModels: List<CollectedModel> = emptyList(),
    val options: List<ModelViewOptions> = emptyList(),
    val clearScene: Boolean = false
)

data class UiState(
    val isLoading: Boolean = true,
    val requestPermissions: Boolean = false,
    val footPrintModel: ModelRenderable? = null,
    val model: ModelRenderable? = null,
    val options: List<ModelViewOptions> = emptyList(),
    val clearScene: Boolean = false
)