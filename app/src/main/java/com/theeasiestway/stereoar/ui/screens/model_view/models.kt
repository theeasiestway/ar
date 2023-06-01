package com.theeasiestway.stereoar.ui.screens.model_view

import com.google.ar.sceneform.rendering.ModelRenderable
import com.theeasiestway.stereoar.ui.screens.models_explorer.FileUri


sealed interface ModelViewOptions {
    data class SaveToCollection(val modelUri: FileUri): ModelViewOptions
    object ClearScene: ModelViewOptions
    object AppSettings: ModelViewOptions
}

sealed interface ModelLoadingStatus {
    data class Progress(
        val downloadId: Long,
        val downloadedBytes: Long,
        val totalBytes: Long
    ): ModelLoadingStatus {
        fun toIntProgress(): Int {
            return (100 * downloadedBytes / totalBytes).toInt()
        }
    }
    data class Done(
        val modelUri: FileUri,
        val model: ModelRenderable,
        val footPrintModel: ModelRenderable
    ): ModelLoadingStatus
}

enum class ModelViewResult {
    CollectedModelsChanged
}