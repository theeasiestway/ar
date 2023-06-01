package com.theeasiestway.domain.repositories.downloads

import com.theeasiestway.domain.repositories.downloads.models.Download
import kotlinx.coroutines.flow.Flow

interface DownloadsRepository {
    suspend fun downloadFile(
        url: String,
        folderToSave: String,
        fileNameToSave: String,
        cancelPrevDownloadsForSaveFolder: Boolean = false,
        notificationTitle: String? = null,
    ): Flow<Download>
    fun cancelDownload(downloadId: Long): Boolean
}