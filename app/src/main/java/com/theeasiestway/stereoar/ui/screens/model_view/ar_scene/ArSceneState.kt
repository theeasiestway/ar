package com.theeasiestway.stereoar.ui.screens.model_view.ar_scene

import com.google.ar.sceneform.rendering.ModelRenderable

data class ArSceneState(
    val footPrintModel: ModelRenderable,
    val model: ModelRenderable,
    val clearScene: Boolean
)