package com.theeasiestway.data.repositories

import android.content.Context
import android.net.Uri
import com.google.ar.sceneform.rendering.ModelRenderable
import com.theeasiestway.domain.exceptions.LoadArModelException
import com.theeasiestway.domain.repositories.ModelsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ModelsRepositoryImpl(
    context: Context,
    private val dispatcher: CoroutineDispatcher
): ModelsRepository<ModelRenderable> {

    private val context = context.applicationContext

    @Throws(Exception::class)
    override suspend fun loadModel(modelUri: String): ModelRenderable? {
        return withContext(dispatcher) {
            tryToLoad(modelUri)
        }
    }

    private suspend fun tryToLoad(modelUri: String, isFilament: Boolean = true): ModelRenderable? {
        return try {
            load(
                modelUri = modelUri,
                isFilament = isFilament
            ).getOrThrow()
        } catch (e: Throwable) {
            e.printStackTrace()
            if (!isFilament) {
                throw e
            } else {
                tryToLoad(modelUri = modelUri, isFilament = false)
            }
        }
    }

    private suspend fun load(modelUri: String, isFilament: Boolean): Result<ModelRenderable> {
        return suspendCoroutine { continuation ->
            ModelRenderable.builder()
                .setSource(context, Uri.parse(modelUri))
                .setIsFilamentGltf(isFilament)
                .build()
                .thenAccept { model ->
                    continuation.resume(Result.success(model))
                }
                .exceptionally { exception ->
                    val error = LoadArModelException("Error loading AR model from uri: $modelUri", exception)
                    continuation.resume(Result.failure(error))
                    null
                }
        }
    }
}