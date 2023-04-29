package com.theeasiestway.data.interactors

import com.google.ar.sceneform.rendering.ModelRenderable
import com.theeasiestway.domain.interactors.GetArModelsInteractor
import com.theeasiestway.domain.repositories.ArModelsRepository

/**
 * Created by Alexey Loboda on 05.02.2022
 */
class GetArModelsInteractorImpl(
    private val repository: ArModelsRepository<ModelRenderable>
): GetArModelsInteractor<ModelRenderable> {

    override suspend fun getAllModels(): List<String> {
        return repository.getAllModels()
    }

    override suspend fun getModel(uri: String): Result<ModelRenderable> {
        return repository.getModel(uri)
    }
}