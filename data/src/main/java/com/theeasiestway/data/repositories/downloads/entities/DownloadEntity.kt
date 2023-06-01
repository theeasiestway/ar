package com.theeasiestway.data.repositories.downloads.entities

@JvmInline
value class DownloadedBytesEntity(val bytes: Long)

@JvmInline
value class TotalBytesEntity(val bytes: Long)

sealed interface DownloadStatusEntity {
    data class Progress(
        val downloadedBytes: DownloadedBytesEntity,
        val totalBytes: TotalBytesEntity
    ): DownloadStatusEntity

    data class Success(
        val fileUri: String
    ): DownloadStatusEntity

    data class Error(
        val reason: Exception
    ): DownloadStatusEntity
}

data class DownloadEntity(
    val id: Long,
    val status: DownloadStatusEntity
)