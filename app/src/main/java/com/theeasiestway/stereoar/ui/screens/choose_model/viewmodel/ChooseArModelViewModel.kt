package com.theeasiestway.stereoar.ui.screens.choose_model.viewmodel

import com.theeasiestway.stereoar.ui.screens.choose_model.FileItem
import com.theeasiestway.stereoar.ui.screens.choose_model.ModelItem
import com.theeasiestway.stereoar.ui.screens.choose_model.PagerPage
import kotlinx.coroutines.flow.Flow

/**
 * Created by Alexey Loboda on 18.07.2022
 */
interface ChooseArModelViewModel {
    fun setModelPath(path: String?)
    fun onFilesPermissionsGranted()
    fun onFileClicked(item: FileItem)
    fun onBackClicked(path: String)
    fun onModelClicked(item: ModelItem)
    fun observePages(): Flow<List<PagerPage>>
    fun observeArModel(): Flow<ModelItem>
}