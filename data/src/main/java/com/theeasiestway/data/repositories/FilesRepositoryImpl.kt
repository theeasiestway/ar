package com.theeasiestway.data.repositories

import android.content.Context
import android.os.Environment
import com.theeasiestway.domain.model.FilesTree
import com.theeasiestway.domain.repositories.FilesRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

class FilesRepositoryImpl(
    context: Context,
    private val dispatcher: CoroutineDispatcher
): FilesRepository {

    companion object {
        private val ROOT_PATH = UUID.randomUUID().toString()
        private const val MODELS_PATH = "models"
        private const val MODELS_COLLECTION_PATH = "$MODELS_PATH/collection"
        private const val EXTERNAL_FILES_DIR_POSTFIX = "/Android/data"
    }

    private val internalFilesDir = Environment.getExternalStorageDirectory()
    private val externalFilesDir = getExternalFilesDir(context)
    private val modelsCollectionDir = File(context.filesDir, MODELS_COLLECTION_PATH)
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

    override suspend fun loadLastVisitedFolder(folderPath: String?): FilesTree {
        return withContext(dispatcher) {
            folderPath?.let { path -> openFolder(path) } ?: rootFilesTree
        }
    }

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

    @Throws(Exception::class, FileAlreadyExistsException::class)
    override suspend fun saveModelToCollection(modelPath: String) {
        withContext(dispatcher) {
            if (!modelsCollectionDir.exists()) {
                modelsCollectionDir.mkdirs()
            }
            val sourceFile = File(modelPath)
            val destFile = File(modelsCollectionDir, sourceFile.name)
            sourceFile.copyTo(destFile)
        }
    }

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