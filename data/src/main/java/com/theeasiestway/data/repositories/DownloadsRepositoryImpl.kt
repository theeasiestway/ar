package com.theeasiestway.data.repositories

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import com.theeasiestway.domain.exceptions.files.FileAlreadyExistsException
import com.theeasiestway.domain.exceptions.files.InsufficientSpaceException
import com.theeasiestway.domain.model.Download
import com.theeasiestway.domain.repositories.DownloadsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import kotlin.coroutines.CoroutineContext

class DownloadsRepositoryImpl(
    context: Context
): DownloadsRepository, CoroutineScope {

    override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.IO

    private val context = context.applicationContext
    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    private val downloadsObserver = MutableSharedFlow<Download>(extraBufferCapacity = 10)
    private val intentFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
    private val broadcastReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            handleDownload(downloadId)
        }
    }

    override fun startObservingDownloads() {
        context.registerReceiver(broadcastReceiver, intentFilter)
    }

    override fun stopObservingDownloads() {
        context.unregisterReceiver(broadcastReceiver)
    }

    override suspend fun downloadFile(url: String, folderToSave: String, fileNameToSave: String): Flow<String> {
        val folder = File(folderToSave)
        val file = File(folder, fileNameToSave)
        if (!folder.exists()) {
            folder.mkdirs()
        }
        val request = DownloadManager.Request(Uri.parse(url))
            .setDestinationUri(Uri.fromFile(file))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
        val downloadId = downloadManager.enqueue(request)
        return downloadsObserver
            .filter { download -> download.id == downloadId }
            .map { download -> download.status.getOrThrow() }
            .map { file.absolutePath }
            .distinctUntilChanged()
    }

    private fun handleDownload(downloadId: Long) {
        if (downloadId < 0) return
        launch {
            try {
                val cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadId))
                val statusColumnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                val reasonColumnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
                var download: Download? = null
                if (cursor.moveToFirst() && statusColumnIndex >= 0 && reasonColumnIndex >= 0) {
                    when (cursor.getInt(statusColumnIndex)) {
                        DownloadManager.STATUS_FAILED -> {
                            val exception = reasonToException(cursor.getInt(reasonColumnIndex))
                            download = Download(
                                id = downloadId,
                                status = Result.failure(exception)
                            )
                        }
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            download = Download(
                                id = downloadId,
                                status = Result.success(Unit)
                            )
                        }
                    }
                }
                cursor.close()
                if (download != null) {
                    downloadsObserver.emit(download)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    private fun reasonToException(reason: Int): Exception {
        return when(reason) {
            DownloadManager.ERROR_FILE_ALREADY_EXISTS -> FileAlreadyExistsException()
            DownloadManager.ERROR_INSUFFICIENT_SPACE -> InsufficientSpaceException()
            else -> Exception("Failed to download the file, reason: $reason")
        }
    }
}