package com.theeasiestway.data.repositories.files.data_store

import com.theeasiestway.data.repositories.files.entities.CollectedFileEntity
import com.theeasiestway.data.repositories.files.entities.FileUriEntity
import com.theeasiestway.data.repositories.files.entities.FilesTreeEntity

interface FilesDataStore {
    suspend fun loadLastVisitedFolder(folderPath: String?): FilesTreeEntity
    suspend fun openFolder(folderPath: String): FilesTreeEntity
    suspend fun saveToCollection(file: FileUriEntity): String
    suspend fun loadCollectedFiles(): List<CollectedFileEntity>
    suspend fun renameCollectedFile(newName: String, file: CollectedFileEntity)
    suspend fun deleteCollectedFile(file: CollectedFileEntity)
    fun loadFilesCollectionPath(): String
    fun loadNewFileNameToCollect(): String
}