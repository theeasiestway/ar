package com.theeasiestway.stereoar.ui.screens.model_view

import com.google.ar.sceneform.rendering.ModelRenderable

data class State(
    val isLoading: Boolean = false,
    val addedToCollection: Boolean = false,
    val model: ModelRenderable? = null
)

data class UiState(
    val isLoading: Boolean = false,
    val addedToCollection: Boolean = false,
    val model: ModelRenderable? = null
)