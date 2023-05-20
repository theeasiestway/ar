package com.theeasiestway.domain.repositories

import kotlinx.coroutines.flow.Flow

interface DownloadsRepository {
    fun startObservingDownloads()
    fun stopObservingDownloads()
    suspend fun downloadFile(
        url: String,
        folderToSave: String,
        fileNameToSave: String,
        progressTitle: String? = null,
    ): Flow<String>
}