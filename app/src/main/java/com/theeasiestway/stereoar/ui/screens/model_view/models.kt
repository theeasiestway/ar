package com.theeasiestway.stereoar.ui.screens.model_view

import com.theeasiestway.stereoar.ui.screens.models_explorer.ModelUri


sealed interface ModelViewOptions {
    data class SaveToCollection(val modelUri: ModelUri.File): ModelViewOptions
    object AppSettings: ModelViewOptions
}

enum class ModelViewResult {
    CollectedModelsChanged
}