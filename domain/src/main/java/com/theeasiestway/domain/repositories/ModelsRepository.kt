package com.theeasiestway.domain.repositories

interface ModelsRepository<T> {
    suspend fun loadModel(modelUri: String): T?
}