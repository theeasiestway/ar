package com.theeasiestway.domain.repositories.downloads.models

@JvmInline
value class DownloadedBytes(val bytes: Long)

@JvmInline
value class TotalBytes(val bytes: Long)

sealed interface DownloadStatus {
    data class Progress(
        val downloadedBytes: DownloadedBytes,
        val totalBytes: TotalBytes
    ): DownloadStatus

    data class Success(
        val fileUri: String
    ): DownloadStatus

    data class Error(
        val reason: Exception
    ): DownloadStatus
}

data class Download(
    val id: Long,
    val status: DownloadStatus
)