package com.theeasiestway.data.repositories

import android.content.Context
import android.os.Environment
import com.theeasiestway.domain.model.FilesTree
import com.theeasiestway.domain.model.FileUri
import com.theeasiestway.domain.repositories.DownloadsRepository
import com.theeasiestway.domain.repositories.FilesRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

class FilesRepositoryImpl(
    context: Context,
    private val downloadsRepository: DownloadsRepository,
    private val dispatcher: CoroutineDispatcher,
): FilesRepository {

    companion object {
        private val ROOT_PATH = UUID.randomUUID().toString()
        private const val MODELS_PATH = "models"
        private const val MODELS_COLLECTION_PATH = "$MODELS_PATH/collection"
        private const val EXTERNAL_FILES_DIR_POSTFIX = "/Android/data"
        private const val DEFAULT_DOWNLOADED_MODEL_NAME = "Model"
        private const val NEW_DOWNLOADED_MODEL_NAME = "$DEFAULT_DOWNLOADED_MODEL_NAME %s"
        private val DOWNLOADED_MODELS_SPLITTER = "$DEFAULT_DOWNLOADED_MODEL_NAME +".toRegex()
    }

    private val internalFilesDir = Environment.getExternalStorageDirectory()
    private val externalFilesDir = getExternalFilesDir(context)
    private val modelsCollectionDir = File(context.getExternalFilesDir(null), MODELS_COLLECTION_PATH)
    private val rootFilesTree = FilesTree(
        rootPath = ROOT_PATH,
        internalStorageRootPath = internalFilesDir.absolutePath,
        externalStorageRootPath = externalFilesDir?.absolutePath,
        currentPath = ROOT_PATH,
        parentPath = null,
        files = listOfNotNull(
            internalFilesDir,
            externalFilesDir
        )
    )
    private var lastCollectedModelNumber = -1

    @Throws(Exception::class)
    override suspend fun loadLastVisitedFolder(folderPath: String?): FilesTree {
        return withContext(dispatcher) {
            folderPath?.let { path -> openFolder(path) } ?: rootFilesTree
        }
    }

    @Throws(Exception::class)
    override suspend fun openFolder(folderPath: String): FilesTree {
        return withContext(dispatcher) {
            if (isRootFolder(folderPath)) rootFilesTree
            else {
                val file = File(folderPath)
                FilesTree(
                    rootPath = ROOT_PATH,
                    internalStorageRootPath = internalFilesDir.absolutePath,
                    externalStorageRootPath = externalFilesDir?.absolutePath,
                    currentPath = file.absolutePath,
                    parentPath = file.parentFile?.absolutePath,
                    files = file.listFiles()?.toList() ?: emptyList(),
                )
            }
        }
    }

    @Throws(
        Exception::class,
        FileAlreadyExistsException::class
    )
    override suspend fun saveModelToCollection(fileUri: FileUri): String {
        return withContext(dispatcher) {
            if (!modelsCollectionDir.exists()) {
                modelsCollectionDir.mkdirs()
            }
            when (fileUri) {
                is FileUri.File -> saveModel(fileUri.uri)
                is FileUri.Url -> {
                    updateLastCollectedModelNumber()
                    downloadsRepository.downloadFile(
                        url = fileUri.uri,
                        folderToSave = modelsCollectionDir.absolutePath,
                        fileNameToSave = NEW_DOWNLOADED_MODEL_NAME.format(++lastCollectedModelNumber) // .format() to make name creation more clear
                    ).first()
                }
            }
        }
    }

    @Throws(Exception::class)
    override suspend fun removeModelFromCollection(fileUri: FileUri.File) {
        withContext(dispatcher) {
            val file = File(fileUri.uri)
            if (file.exists() && !file.isDirectory) {
                file.delete()
            }
        }
    }

    private suspend fun updateLastCollectedModelNumber() {
        if (lastCollectedModelNumber < 0) {
            loadModelsFromCollection()
                .mapNotNull { file ->
                    file.name
                        .split(DOWNLOADED_MODELS_SPLITTER)
                        .getOrNull(1)
                        ?.toIntOrNull()
                }.maxOrNull()
                ?.let { lastModelNumber ->
                    lastCollectedModelNumber = lastModelNumber
                }
        }
    }

    @Throws(Exception::class)
    private suspend fun saveModel(modelPath: String): String {
        return withContext(dispatcher) {
            val sourceFile = File(modelPath)
            val destFile = File(modelsCollectionDir, sourceFile.name)
            sourceFile.copyTo(destFile)
            destFile.name
        }
    }

    @Throws(Exception::class)
    override suspend fun loadModelsFromCollection(): List<File> {
        return withContext(dispatcher) {
            if (modelsCollectionDir.exists()) {
                modelsCollectionDir.listFiles()?.toList() ?: emptyList()
            } else emptyList()
        }
    }

    private fun getExternalFilesDir(context: Context): File? {
        val externalStoragePath: String? = if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED ||
            Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED_READ_ONLY) {
            context.getExternalFilesDirs(null)
                .firstOrNull { dir ->
                    !dir.absolutePath.contains(internalFilesDir.absolutePath, true)
                }?.run {
                    absolutePath
                        .split(EXTERNAL_FILES_DIR_POSTFIX, ignoreCase = true)
                        .firstOrNull()
                }
        } else null
        return externalStoragePath?.let { path ->
            File(path)
        }
    }

    private fun isRootFolder(folderPath: String): Boolean { // for handling back from some folder to root
        return (internalFilesDir.absolutePath.startsWith(folderPath) && folderPath.length < internalFilesDir.absolutePath.length) ||
                (externalFilesDir != null && externalFilesDir.absolutePath.startsWith(folderPath) && folderPath.length < externalFilesDir.absolutePath.length)
    }
}