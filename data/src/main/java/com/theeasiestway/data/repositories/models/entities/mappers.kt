package com.theeasiestway.data.repositories.models.entities

import com.google.ar.sceneform.rendering.ModelRenderable

fun ModelEntity.toDomain(): ModelRenderable {
    return model
}