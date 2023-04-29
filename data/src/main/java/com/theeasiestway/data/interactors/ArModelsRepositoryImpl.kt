package com.theeasiestway.data.interactors

import android.content.Context
import android.net.Uri
import com.google.ar.sceneform.rendering.ModelRenderable
import com.theeasiestway.data.R
import com.theeasiestway.data.mappers.rawIdToString
import com.theeasiestway.domain.exceptions.LoadArModelException
import com.theeasiestway.domain.repositories.ArModelsRepository
import java.lang.reflect.Field
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Created by Alexey Loboda on 06.02.2022
 */
class ArModelsRepositoryImpl(context: Context): ArModelsRepository<ModelRenderable> {

    private val modelsFolder = "models"
    private val context = context.applicationContext

    override suspend fun getModel(uri: String): Result<ModelRenderable> {
        return suspendCoroutine { continuation ->
            ModelRenderable.builder()
                .setSource(context, Uri.parse(uri))
                .setIsFilamentGltf(true) // todo check it might also need false value
                .build()
                .thenAccept { continuation.resume(Result.success(it)) }
                .exceptionally {
                    val error = LoadArModelException("Error while loading AR model from uri: $uri", it)
                    continuation.resume(Result.failure(error))
                    null
                }
        }
    }

    override suspend fun getAllModels(): List<String> {
        return suspendCoroutine { continuation ->
            getAllRawFilesIds().map { rawIdToString(it, context.resources) }
                .filter { !it.contains("sceneform", true) && !it.contains("keep_arcore", true) }
                .also {
                    continuation.resume(it)
                }
        }
    }

    private fun getAllRawFilesIds(): List<Int> {
        val resIds = mutableListOf<Int>()
        val fields: Array<Field> = R.raw::class.java.fields
        for (field in fields) {
            try {
                resIds.add(field.getInt(field))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return resIds
    }
}