package com.theeasiestway.domain.repositories.models

interface ModelsRepository<T> {
    suspend fun getModel(modelUri: String): T?
}