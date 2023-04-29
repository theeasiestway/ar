package com.theeasiestway.stereoar.ui.screens.arview

import android.Manifest
import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.ar.sceneform.rendering.ModelRenderable
import com.theeasiestway.data.interactors.ArModelsRepositoryImpl
import com.theeasiestway.data.interactors.GetArModelsInteractorImpl
import com.theeasiestway.data.mappers.rawIdToString
import com.theeasiestway.domain.interactors.GetArModelsInteractor
import com.theeasiestway.domain.repositories.ArModelsRepository
import com.theeasiestway.stereoar.app.App
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import com.google.ar.sceneform.ux.R as SceneformUxRes

/**
 * Created by Alexey Loboda on 29.01.2022
 */
class ArViewModel(application: Application): AndroidViewModel(application) {

    private val arModelsRepository: ArModelsRepository<ModelRenderable> = ArModelsRepositoryImpl(application)
    private val getArModelsInteractor: GetArModelsInteractor<ModelRenderable> = GetArModelsInteractorImpl(arModelsRepository)
    private val _arModelsFlow = MutableStateFlow<List<String>>(emptyList())
    private val _footprintArModel = MutableStateFlow<Result<ModelRenderable>?>(null)
    private val arModelsFlow = _arModelsFlow.asSharedFlow()
    private val footprintArModel = _footprintArModel.asSharedFlow()

    init {
        loadFootprintArModel()
        loadArModelsUris()
    }

    private fun loadArModelsUris() {
        viewModelScope.launch {
            _arModelsFlow.tryEmit(getArModelsInteractor.getAllModels())
        }
    }

    private fun loadFootprintArModel() {
        viewModelScope.launch {
            _footprintArModel.tryEmit(
                getArModelsInteractor.getModel(rawIdToString(SceneformUxRes.raw.sceneform_footprint, getApplication<App>().resources))
            )
        }
    }

    fun getFootprintArModel(): Flow<Result<ModelRenderable>> {
        return footprintArModel.filterNotNull()
    }

    fun getAllArModels(): Flow<List<String>> {
        return arModelsFlow
    }

    fun onModelClicked(uri: String, onComplete: (Result<ModelRenderable>) -> Unit) {
        viewModelScope.launch {
            onComplete(getArModelsInteractor.getModel(uri))
        }
    }

    fun getRandomModel(onComplete: (ModelRenderable?) -> Unit) {
        viewModelScope.launch {
            onComplete(getArModelsInteractor.getModel(arModelsFlow.replayCache.first().random()).getOrNull())
        }
    }
}