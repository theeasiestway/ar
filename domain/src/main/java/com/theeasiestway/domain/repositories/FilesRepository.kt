package com.theeasiestway.domain.repositories

import com.theeasiestway.domain.model.CollectedModel
import com.theeasiestway.domain.model.FileUri
import com.theeasiestway.domain.model.FilesTree

interface FilesRepository {
    suspend fun loadLastVisitedFolder(folderPath: String?): FilesTree
    suspend fun openFolder(folderPath: String): FilesTree
    suspend fun saveModelToCollection(fileUri: FileUri.File): String
    suspend fun loadModelsFromCollection(): List<CollectedModel>
    suspend fun renameCollectedModel(newName: String, model: CollectedModel)
    suspend fun deleteModelFromCollection(model: CollectedModel)
    fun getModelsCollectionPath(): String
    fun getNewModelNameToCollect(): String
}