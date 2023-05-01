package com.theeasiestway.stereoar.di

import com.google.ar.sceneform.rendering.ModelRenderable
import com.theeasiestway.data.repositories.ModelsRepositoryImpl
import com.theeasiestway.domain.repositories.ModelsRepository
import com.theeasiestway.stereoar.ui.screens.model_view.ModelViewViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

const val modelViewScopeId = "modelViewScopeId"
val modelViewModule = module {

    scope(named(modelViewScopeId)) {
        scoped<ModelsRepository<ModelRenderable>> {
            ModelsRepositoryImpl(
                context = androidContext(),
                dispatcher = get(named(ioDispatcher))
            )
        }
    }

    viewModel {
        val scope = getScope(modelViewScopeId)
        ModelViewViewModel(
            modelsRepository = scope.get(),
            filesRepository = get(),
            dispatcher = get(named(ioDispatcher))
        )
    }
}