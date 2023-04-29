package com.theeasiestway.domain.repositories

/**
 * Created by Alexey Loboda on 06.02.2022
 */
interface ArModelsRepository<M> {
    suspend fun getModel(uri: String): Result<M>
    suspend fun getAllModels(): List<String>
}