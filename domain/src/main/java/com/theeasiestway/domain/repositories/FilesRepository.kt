package com.theeasiestway.domain.repositories

import com.theeasiestway.domain.model.FilesTree
import java.io.File

interface FilesRepository {
    suspend fun loadLastVisitedFolder(folderPath: String?): FilesTree
    suspend fun openFolder(folderPath: String): FilesTree
    suspend fun saveModelToCollection(modelPath: String)
    suspend fun loadModelsFromCollection(): List<File>
}