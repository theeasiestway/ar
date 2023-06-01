package com.theeasiestway.data.repositories.models

import com.google.ar.sceneform.rendering.ModelRenderable
import com.theeasiestway.data.repositories.models.data_store.ModelsDataStore
import com.theeasiestway.data.repositories.models.entities.toDomain
import com.theeasiestway.domain.repositories.models.ModelsRepository

class ModelsRepositoryImpl(
    private val dataStore: ModelsDataStore
): ModelsRepository<ModelRenderable> {

    @Throws(Exception::class)
    override suspend fun getModel(modelUri: String): ModelRenderable? {
        return dataStore.loadModel(modelUri)?.toDomain()
    }
}