package com.theeasiestway.data.repositories.models.entities

import com.google.ar.sceneform.rendering.ModelRenderable

data class ModelEntity(
    val modelUri: String,
    val model: ModelRenderable
)