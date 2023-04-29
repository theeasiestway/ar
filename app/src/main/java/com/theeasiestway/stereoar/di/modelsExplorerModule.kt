package com.theeasiestway.stereoar.di

import com.theeasiestway.data.repositories.FilesRepositoryImpl
import com.theeasiestway.data.repositories.SettingsRepositoryImpl
import com.theeasiestway.domain.repositories.FilesRepository
import com.theeasiestway.domain.repositories.SettingsRepository
import com.theeasiestway.stereoar.ui.screens.models_explorer.ModelsExplorerViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

const val modelsExplorerScopeId = "modelsExplorerScopeId"
val modelsExplorerModule = module {

    scope(named(modelsExplorerScopeId)) {
        scoped<SettingsRepository> {
            SettingsRepositoryImpl(androidContext())
        }
        scoped<FilesRepository> {
            FilesRepositoryImpl(androidContext())
        }
    }

    viewModel {
        val scope = getScope(modelsExplorerScopeId)
        ModelsExplorerViewModel(scope.get(), scope.get(), scope.get())
    }
}