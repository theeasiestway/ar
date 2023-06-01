package com.theeasiestway.data.repositories.downloads.entities

import com.theeasiestway.domain.repositories.downloads.models.Download
import com.theeasiestway.domain.repositories.downloads.models.DownloadStatus
import com.theeasiestway.domain.repositories.downloads.models.DownloadedBytes
import com.theeasiestway.domain.repositories.downloads.models.TotalBytes

fun DownloadStatusEntity.toDomain(): DownloadStatus {
    return when(this) {
        is DownloadStatusEntity.Progress -> DownloadStatus.Progress(
            DownloadedBytes(downloadedBytes.bytes),
            TotalBytes(totalBytes.bytes)
        )
        is DownloadStatusEntity.Success -> DownloadStatus.Success(fileUri)
        is DownloadStatusEntity.Error -> DownloadStatus.Error(reason)
    }
}
fun DownloadEntity.toDomain(): Download {
    return Download(
        id = id,
        status = status.toDomain()
    )
}