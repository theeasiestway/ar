package com.theeasiestway.data.repositories

import android.content.Context
import android.net.Uri
import androidx.annotation.MainThread
import com.google.ar.sceneform.rendering.ModelRenderable
import com.theeasiestway.data.R
import com.theeasiestway.data.mappers.toUri
import com.theeasiestway.domain.exceptions.LoadArModelException
import com.theeasiestway.domain.repositories.ModelsRepository
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class ModelsRepositoryImpl(
    context: Context
): ModelsRepository<ModelRenderable> {

    private val context = context.applicationContext
    private val footPrintModel: ModelRenderable? = null

    @Throws(Exception::class)
    @MainThread // ARCore requirement
    override suspend fun loadModel(modelUri: String): ModelRenderable? {
        return tryToLoad(modelUri)
    }

    @MainThread // ARCore requirement
    override suspend fun loadFootPrintModel(): ModelRenderable {
        return footPrintModel ?: loadModel(R.raw.sceneform_footprint.toUri(context.resources))!!
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
                    continuation.resumeWithException(error)
                    null
                }
        }
    }
}