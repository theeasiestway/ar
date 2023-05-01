package com.theeasiestway.domain.repositories

import com.theeasiestway.domain.model.FilesTree
import com.theeasiestway.domain.model.FileUri
import java.io.File

interface FilesRepository {
    suspend fun loadLastVisitedFolder(folderPath: String?): FilesTree
    suspend fun openFolder(folderPath: String): FilesTree
    suspend fun saveModelToCollection(fileUri: FileUri): String
    suspend fun removeModelFromCollection(fileUri: FileUri.File)
    suspend fun loadModelsFromCollection(): List<File>
}