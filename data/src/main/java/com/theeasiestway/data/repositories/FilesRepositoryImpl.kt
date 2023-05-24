package com.theeasiestway.data.repositories

import android.content.Context
import android.os.Environment
import com.theeasiestway.domain.model.CollectedModel
import com.theeasiestway.domain.model.FileUri
import com.theeasiestway.domain.model.FilesTree
import com.theeasiestway.domain.repositories.FilesRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

class FilesRepositoryImpl(
    context: Context,
    private val dispatcher: CoroutineDispatcher,
): FilesRepository {

    companion object {
        private val ROOT_PATH = UUID.randomUUID().toString()
        private const val MODELS_PATH = "models"
        private const val MODELS_COLLECTION_PATH = "$MODELS_PATH/collection"
        private const val EXTERNAL_FILES_DIR_POSTFIX = "/Android/data"
        private const val DEFAULT_SAVED_MODEL_NAME = "Model"
        private const val DEFAULT_DOWNLOADED_NAME = "temp"
        private const val NEW_DOWNLOADED_MODEL_NAME = "${DEFAULT_SAVED_MODEL_NAME}_${DEFAULT_DOWNLOADED_NAME}_%d"
        private const val NEW_SAVED_MODEL_NAME = "$DEFAULT_SAVED_MODEL_NAME %d"
        private val DOWNLOADED_MODELS_NAME_PATTERN = "_${DEFAULT_DOWNLOADED_NAME}_\\d{8,}".toRegex()
        private val SAVED_MODELS_NAME_PATTERN = "$DEFAULT_SAVED_MODEL_NAME +".toRegex()
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
    override suspend fun saveModelToCollection(fileUri: FileUri.File): String {
        return withContext(dispatcher) {
            if (!modelsCollectionDir.exists()) {
                modelsCollectionDir.mkdirs()
            }
            if (fileUri.uri.contains(DOWNLOADED_MODELS_NAME_PATTERN)) {
                deleteAllTempModelsExcept(fileUri.uri)
                updateLastCollectedModelNumber()
                val sourceFile = File(fileUri.uri)
                val newName = NEW_SAVED_MODEL_NAME.format(++lastCollectedModelNumber)
                val newFile = File(sourceFile.absolutePath.substringBeforeLast("/"), newName)
                sourceFile.renameTo(newFile)
                newFile.name
            } else {
                val sourceFile = File(fileUri.uri)
                val destFile = File(modelsCollectionDir, sourceFile.name)
                sourceFile.copyTo(destFile)
                destFile.name
            }
        }
    }

    private suspend fun deleteAllTempModelsExcept(exceptUri: String) {
        loadSavedModels(filterTempModels = false)
            .forEach { model ->
                if (model.path != exceptUri &&
                    model.path.contains(DOWNLOADED_MODELS_NAME_PATTERN)
                ) {
                    deleteModelFromCollection(model)
                }
            }
    }

    private suspend fun updateLastCollectedModelNumber() {
        if (lastCollectedModelNumber < 0) {
            loadSavedModels(filterTempModels = true)
                .mapNotNull { file ->
                    file.name
                        .split(SAVED_MODELS_NAME_PATTERN)
                        .getOrNull(1)
                        ?.toIntOrNull()
                }.maxOrNull()
                ?.let { lastModelNumber ->
                    lastCollectedModelNumber = lastModelNumber
                }
        }
    }

    @Throws(Exception::class)
    override suspend fun loadModelsFromCollection(): List<CollectedModel> {
        return loadSavedModels(filterTempModels = true)
    }

    private suspend fun loadSavedModels(filterTempModels: Boolean): List<CollectedModel> {
        return withContext(dispatcher) {
            if (modelsCollectionDir.exists()) {
                modelsCollectionDir.listFiles()
                    ?.filter { file ->
                        if (filterTempModels) {
                            !file.name.contains(DOWNLOADED_MODELS_NAME_PATTERN)
                        } else true
                    }
                    ?.map { file ->
                        CollectedModel(
                            name = file.name,
                            path = file.absolutePath,
                            sizeBytes = file.length(),
                            creationDateMillis = file.lastModified()
                        )
                    }
                    ?: emptyList()
            } else emptyList()
        }
    }

    @Throws(Exception::class)
    override suspend fun renameCollectedModel(newName: String, model: CollectedModel) {
        withContext(dispatcher) {
            val file = File(model.path)
            if (file.exists() && file.name != newName) {
                val newFile = File(model.path.substringBeforeLast("/"), newName)
                file.renameTo(newFile)
            }
        }
    }

    @Throws(Exception::class)
    override suspend fun deleteModelFromCollection(model: CollectedModel) {
        withContext(dispatcher) {
            val file = File(model.path)
            if (file.exists() && !file.isDirectory) {
                file.delete()
            }
        }
    }

    override fun getModelsCollectionPath(): String {
        return modelsCollectionDir.absolutePath
    }

    override fun getNewModelNameToCollect(): String {
        return NEW_DOWNLOADED_MODEL_NAME.format(System.currentTimeMillis())
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