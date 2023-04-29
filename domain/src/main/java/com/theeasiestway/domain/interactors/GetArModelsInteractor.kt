package com.theeasiestway.domain.interactors

/**
 * Created by Alexey Loboda on 03.02.2022
 */
interface GetArModelsInteractor<M> {
    suspend fun getModel(uri: String): Result<M>
    suspend fun getAllModels(): List<String>
}