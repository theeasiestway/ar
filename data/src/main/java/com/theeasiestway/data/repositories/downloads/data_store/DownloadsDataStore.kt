package com.theeasiestway.data.repositories.downloads.data_store

import com.theeasiestway.data.repositories.downloads.entities.DownloadEntity
import kotlinx.coroutines.flow.Flow

interface DownloadsDataStore {
    suspend fun downloadFile(
        url: String,
        folderToSave: String,
        fileNameToSave: String,
        cancelPrevDownloadsForSaveFolder: Boolean = false,
        notificationTitle: String? = null,
    ): Flow<DownloadEntity>
    fun cancelDownload(downloadId: Long): Boolean
}