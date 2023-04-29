package com.theeasiestway.stereoar.ui.screens.choose_model.viewmodel

import com.theeasiestway.stereoar.R
import com.theeasiestway.stereoar.ui.screens.choose_model.FileItem
import com.theeasiestway.stereoar.ui.screens.choose_model.ModelItem
import com.theeasiestway.stereoar.ui.screens.choose_model.PagerPage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import java.util.*

/**
 * Created by Alexey Loboda on 18.07.2022
 */
class ChooseArModelViewModelStub: ChooseArModelViewModel {

    override fun setModelPath(path: String?) {

    }

    override fun onFilesPermissionsGranted() {

    }

    override fun onFileClicked(item: FileItem) {

    }

    override fun onBackClicked(path: String) {

    }

    override fun onModelClicked(item: ModelItem) {

    }

    override fun observePages(): Flow<List<PagerPage>> {
        return flow {
            listOf(
                PagerPage.FilesExplorerState(
                    R.string.models_explorer_title,
                    "displayablePath",
                    "currentPath",
                    mutableListOf<FileItem>().apply {
                        repeat(10) {
                            add(
                                if (it % 2 == 0) FileItem.File(
                                    parentPath = "Parent path ${it - 1}",
                                    path = "File path $it",
                                    title = "File name $it",
                                    icon = R.drawable.ic_file,
                                    creationDate = Date().toString(),
                                    size = "${it * 1000} Bytes"
                                )
                                else FileItem.Folder(
                                    parentPath = "Parent path ${it - 1}",
                                    path = "Folder path $it",
                                    title = "Folder name $it",
                                    icon = R.drawable.ic_folder,
                                    creationDate = Date().toString(),
                                    itemsCount = "$it"
                                )
                            )
                        }
                    },
                    false
                ),
                PagerPage.FavoritesModelsState(
                    R.string.choose_ar_model_favorite_models,
                    mutableListOf<ModelItem>().apply {
                        repeat(10) {
                            add(
                                ModelItem(
                                    path = "Model path $it",
                                    title = "Model title $it",
                                    size = "${it * 1000} Bytes",
                                    creationDate = Date().toString()
                                )
                            )
                        }
                    }
                )
            )
        }
    }

    override fun observeArModel(): Flow<ModelItem> {
        return emptyFlow()
    }
}