package com.theeasiestway.data.repositories.models.data_store

import com.theeasiestway.data.repositories.models.entities.ModelEntity

interface ModelsDataStore {
    suspend fun loadModel(modelUri: String): ModelEntity?
}