package com.theeasiestway.data.repositories.models.data_store

import android.content.Context
import android.net.Uri
import androidx.annotation.MainThread
import com.google.ar.sceneform.rendering.ModelRenderable
import com.theeasiestway.data.repositories.models.entities.ModelEntity
import com.theeasiestway.domain.repositories.models.errors.LoadArModelException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class ModelsLocalDataStore(
    context: Context
): ModelsDataStore {

    private val context = context.applicationContext

    @Throws(Exception::class)
    @MainThread // ARCore requirement
    override suspend fun loadModel(modelUri: String): ModelEntity? {
        return tryToLoad(modelUri)
    }

    private suspend fun tryToLoad(modelUri: String, isFilament: Boolean = true): ModelEntity? {
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
                tryToLoad(
                    modelUri = modelUri,
                    isFilament = false
                )
            }
        }
    }

    private suspend fun load(modelUri: String, isFilament: Boolean): Result<ModelEntity> {
        return suspendCoroutine { continuation ->
            ModelRenderable.builder()
                .setSource(context, Uri.parse(modelUri))
                .setIsFilamentGltf(isFilament)
                .build()
                .thenAccept { model ->
                    continuation.resume(
                        Result.success(
                            ModelEntity(
                                modelUri = modelUri,
                                model = model
                            )
                        )
                    )
                }
                .exceptionally { exception ->
                    val error = LoadArModelException("Error loading AR model from uri: $modelUri", exception)
                    continuation.resumeWithException(error)
                    null
                }
        }
    }
}