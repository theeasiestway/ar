package com.theeasiestway.stereoar.ui.screens.model_view

import com.google.ar.sceneform.rendering.ModelRenderable
import com.theeasiestway.stereoar.ui.screens.models_explorer.ModelUri

data class State(
    val isLoading: Boolean = true,
    val requestPermissions: Boolean = false,
    val addedToCollection: Boolean = false,
    val modelUri: ModelUri? = null,
    val footPrintModel: ModelRenderable? = null,
    val model: ModelRenderable? = null,
    val showOptions: Boolean = false,
    val clearScene: Boolean = false
)

data class UiState(
    val isLoading: Boolean = true,
    val requestPermissions: Boolean = false,
    val addedToCollection: Boolean = false,
    val footPrintModel: ModelRenderable? = null,
    val model: ModelRenderable? = null,
    val showOptions: Boolean = false,
    val clearScene: Boolean = false
)