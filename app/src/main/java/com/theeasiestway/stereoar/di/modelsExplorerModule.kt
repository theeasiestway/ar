package com.theeasiestway.stereoar.di

import com.theeasiestway.stereoar.ui.screens.models_explorer.ModelsExplorerViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module


val modelsExplorerModule = module {

    viewModel {
        ModelsExplorerViewModel(
            filesRepository = get(),
            settingsRepository = get(),
            dispatcher = get(named(ioDispatcher))
        )
    }
}