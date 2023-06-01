package com.theeasiestway.domain.repositories.files

import com.theeasiestway.domain.repositories.files.models.CollectedModel
import com.theeasiestway.domain.repositories.files.models.FileUri
import com.theeasiestway.domain.repositories.files.models.FilesTree

interface FilesRepository {
    suspend fun getLastVisitedFolder(folderPath: String?): FilesTree
    suspend fun openFolder(folderPath: String): FilesTree
    suspend fun saveToCollection(file: FileUri): String
    suspend fun getCollectedModels(): List<CollectedModel>
    suspend fun renameCollectedModel(newName: String, model: CollectedModel)
    suspend fun deleteCollectedModel(model: CollectedModel)
    fun getModelsCollectionPath(): String
    fun getNewModelNameToCollect(): String
}