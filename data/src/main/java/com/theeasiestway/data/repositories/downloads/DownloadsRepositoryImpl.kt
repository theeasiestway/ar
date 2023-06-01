package com.theeasiestway.data.repositories.downloads

import com.theeasiestway.data.repositories.downloads.data_store.DownloadsDataStore
import com.theeasiestway.data.repositories.downloads.entities.toDomain
import com.theeasiestway.domain.repositories.downloads.models.Download
import com.theeasiestway.domain.repositories.downloads.DownloadsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class DownloadsRepositoryImpl(
    private val dataStore: DownloadsDataStore,
    private val ioDispatcher: CoroutineDispatcher
): DownloadsRepository {

    override suspend fun downloadFile(
        url: String,
        folderToSave: String,
        fileNameToSave: String,
        cancelPrevDownloadsForSaveFolder: Boolean,
        notificationTitle: String?
    ): Flow<Download> {
        return withContext(ioDispatcher) {
            dataStore.downloadFile(
                url,
                folderToSave,
                fileNameToSave,
                cancelPrevDownloadsForSaveFolder
            ).map { downloadEntity -> downloadEntity.toDomain() }
        }
    }

    override fun cancelDownload(downloadId: Long): Boolean {
        return dataStore.cancelDownload(downloadId)
    }
}