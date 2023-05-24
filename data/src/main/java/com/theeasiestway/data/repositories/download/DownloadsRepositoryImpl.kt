package com.theeasiestway.data.repositories.download

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import com.theeasiestway.domain.exceptions.files.FileAlreadyExistsException
import com.theeasiestway.domain.exceptions.files.InsufficientSpaceException
import com.theeasiestway.domain.model.Download
import com.theeasiestway.domain.model.DownloadStatus
import com.theeasiestway.domain.model.DownloadedBytes
import com.theeasiestway.domain.model.TotalBytes
import com.theeasiestway.domain.repositories.DownloadsRepository
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import java.io.File

class DownloadsRepositoryImpl(
    context: Context
): DownloadsRepository {
    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    override suspend fun downloadFile(
        url: String,
        folderToSave: String,
        fileNameToSave: String,
        cancelPrevDownloadsForSaveFolder: Boolean,
        notificationTitle: String?,
    ): Flow<Download> {
        val folder = File(folderToSave)
        val file = File(folder, fileNameToSave)
        if (!folder.exists()) {
            folder.mkdirs()
        }
        val request = DownloadManager.Request(Uri.parse(url))
            .setDestinationUri(Uri.fromFile(file))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
        if (notificationTitle != null) {
            request.setTitle(notificationTitle)
        }
        if (cancelPrevDownloadsForSaveFolder) {
            cancelAllDownloads(folderToSave)
        }
        val downloadId = downloadManager.enqueue(request)
        return trackDownload(
            downloadId = downloadId,
            fileUri = file.absolutePath
        )
    }

    private fun cancelAllDownloads(folderToSave: String) {
        val status = DownloadManager.STATUS_FAILED or
                DownloadManager.STATUS_PENDING or
                DownloadManager.STATUS_PAUSED or
                DownloadManager.STATUS_RUNNING
        val cursor = downloadManager.query(DownloadManager.Query().setFilterByStatus(status))
        val localUriColumnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
        val downloadIdColumnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_ID)
        while (cursor.moveToNext()) {
            if (cursor.getString(localUriColumnIndex)?.contains(folderToSave) == true) {
                val downloadId = cursor.getLong(downloadIdColumnIndex)
                cancelDownload(downloadId)
            }
        }
        cursor.close()
    }

    private suspend fun trackDownload(downloadId: Long, fileUri: String) = flow {
        var stop = false
        while (currentCoroutineContext().isActive && !stop) {
            val cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadId))
            val statusColumnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            val totalBytesColumnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
            val downloadedBytesColumnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
            val reasonColumnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
            if (cursor.moveToFirst()) {
                when (cursor.getInt(statusColumnIndex)) {
                    DownloadManager.STATUS_RUNNING -> {
                        val downloadedBytes = cursor.getLong(downloadedBytesColumnIndex)
                        val totalBytes = cursor.getLong(totalBytesColumnIndex)
                        emit(
                            Download(
                                id = downloadId,
                                status = DownloadStatus.Progress(
                                    downloadedBytes = DownloadedBytes(downloadedBytes),
                                    totalBytes = TotalBytes(totalBytes)
                                )
                            )
                        )
                    }
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        stop = true
                        emit(
                            Download(
                                id = downloadId,
                                status = DownloadStatus.Success(
                                    fileUri = fileUri
                                )
                            )
                        )
                    }
                    DownloadManager.STATUS_FAILED -> {
                        stop = true
                        emit(
                            Download(
                                id = downloadId,
                                status = DownloadStatus.Error(
                                    reason = reasonToException(cursor.getInt(reasonColumnIndex))
                                )
                            )
                        )
                    }
                }
            }
            cursor.close()
            delay(300)
        }
    }.distinctUntilChanged()

    override fun cancelDownload(downloadId: Long): Boolean {
        return downloadManager.remove(downloadId) > 0
    }

    private fun reasonToException(reason: Int): Exception {
        return when(reason) {
            DownloadManager.ERROR_FILE_ALREADY_EXISTS -> FileAlreadyExistsException()
            DownloadManager.ERROR_INSUFFICIENT_SPACE -> InsufficientSpaceException()
            else -> Exception("Failed to download the file, reason: $reason")
        }
    }
}