package com.theeasiestway.data.repositories.files

import com.theeasiestway.data.repositories.files.data_store.FilesDataStore
import com.theeasiestway.data.repositories.files.entities.toDomain
import com.theeasiestway.data.repositories.files.entities.toEntity
import com.theeasiestway.domain.repositories.files.models.CollectedModel
import com.theeasiestway.domain.repositories.files.models.FileUri
import com.theeasiestway.domain.repositories.files.models.FilesTree
import com.theeasiestway.domain.repositories.files.FilesRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class FilesRepositoryImpl(
    private val dataStore: FilesDataStore,
    private val ioDispatcher: CoroutineDispatcher
): FilesRepository {
    override suspend fun getLastVisitedFolder(folderPath: String?): FilesTree {
        return withContext(ioDispatcher) {
            dataStore.loadLastVisitedFolder(folderPath).toDomain()
        }
    }

    override suspend fun openFolder(folderPath: String): FilesTree {
        return withContext(ioDispatcher) {
            dataStore.openFolder(folderPath).toDomain()
        }
    }

    override suspend fun saveToCollection(file: FileUri): String {
        return withContext(ioDispatcher) {
            dataStore.saveToCollection(file.toEntity())
        }
    }

    override suspend fun getCollectedModels(): List<CollectedModel> {
        return withContext(ioDispatcher) {
            dataStore.loadCollectedFiles().toDomain()
        }
    }

    override suspend fun renameCollectedModel(newName: String, model: CollectedModel) {
        withContext(ioDispatcher) {
            dataStore.renameCollectedFile(newName, model.toEntity())
        }
    }

    override suspend fun deleteCollectedModel(model: CollectedModel) {
        withContext(ioDispatcher) {
            dataStore.deleteCollectedFile(model.toEntity())
        }
    }

    override fun getModelsCollectionPath(): String {
        return dataStore.loadFilesCollectionPath()
    }

    override fun getNewModelNameToCollect(): String {
        return dataStore.loadNewFileNameToCollect()
    }
}