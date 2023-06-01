package com.theeasiestway.data.repositories.files.data_store

import android.content.Context
import android.os.Environment
import com.theeasiestway.data.repositories.files.entities.CollectedFileEntity
import com.theeasiestway.data.repositories.files.entities.DirEntity
import com.theeasiestway.data.repositories.files.entities.FileUriEntity
import com.theeasiestway.data.repositories.files.entities.FilesTreeEntity
import java.io.File
import java.util.UUID

class FilesLocalDataStore(
    context: Context
): FilesDataStore {
    companion object {
        private val ROOT_PATH = UUID.randomUUID().toString()
        private const val FILES_PATH = "files"
        private const val FILES_COLLECTION_PATH = "$FILES_PATH/collection"
        private const val EXTERNAL_FILES_DIR_POSTFIX = "/Android/data"
        private const val DEFAULT_SAVED_FILE_NAME = "Model"
        private const val DEFAULT_DOWNLOADED_NAME = "temp"
        private const val NEW_DOWNLOADED_FILE_NAME = "${DEFAULT_SAVED_FILE_NAME}_${DEFAULT_DOWNLOADED_NAME}_%d"
        private const val NEW_SAVED_FILE_NAME = "$DEFAULT_SAVED_FILE_NAME %d"
        private val DOWNLOADED_FILES_NAME_PATTERN = "_${DEFAULT_DOWNLOADED_NAME}_\\d{8,}".toRegex()
        private val SAVED_FILES_NAME_PATTERN = "$DEFAULT_SAVED_FILE_NAME +".toRegex()
    }

    private val internalFilesDir = Environment.getExternalStorageDirectory()
    private val externalFilesDir = getExternalFilesDir(context)
    private val filesCollectionDir = File(context.getExternalFilesDir(null), FILES_COLLECTION_PATH)
    private val rootFilesTree = FilesTreeEntity(
        rootPath = ROOT_PATH,
        internalStorageRootPath = internalFilesDir.absolutePath,
        externalStorageRootPath = externalFilesDir?.absolutePath,
        currentPath = ROOT_PATH,
        parentPath = null,
        files = listOfNotNull(
            internalFilesDir.toFileEntity(),
            externalFilesDir?.toFileEntity()
        )
    )
    private var lastCollectedFileNumber = -1

    @Throws(Exception::class)
    override suspend fun loadLastVisitedFolder(folderPath: String?): FilesTreeEntity {
        return folderPath?.let { path -> openFolder(path) } ?: rootFilesTree
    }

    @Throws(Exception::class)
    override suspend fun openFolder(folderPath: String): FilesTreeEntity {
        return if (isRootFolder(folderPath)) rootFilesTree
        else {
            val file = File(folderPath)
            FilesTreeEntity(
                rootPath = ROOT_PATH,
                internalStorageRootPath = internalFilesDir.absolutePath,
                externalStorageRootPath = externalFilesDir?.absolutePath,
                currentPath = file.absolutePath,
                parentPath = file.parentFile?.absolutePath,
                files = file.listFiles()?.toList()?.toFilesEntities() ?: emptyList(),
            )
        }
    }

    @Throws(Exception::class, FileAlreadyExistsException::class)
    override suspend fun saveToCollection(file: FileUriEntity): String {
        if (!filesCollectionDir.exists()) {
            filesCollectionDir.mkdirs()
        }
        return if (file.uri.contains(DOWNLOADED_FILES_NAME_PATTERN)) {
            deleteAllTempFilesExcept(file.uri)
            updateLastCollectedFileNumber()
            val sourceFile = File(file.uri)
            val newName = NEW_SAVED_FILE_NAME.format(++lastCollectedFileNumber)
            val newFile = File(sourceFile.absolutePath.substringBeforeLast("/"), newName)
            sourceFile.renameTo(newFile)
            newFile.name
        } else {
            val sourceFile = File(file.uri)
            val destFile = File(filesCollectionDir, sourceFile.name)
            sourceFile.copyTo(destFile)
            destFile.name
        }
    }

    private suspend fun deleteAllTempFilesExcept(exceptUri: String) {
        loadSavedFiles(filterTempFiles = false)
            .forEach { file ->
                if (file.path != exceptUri &&
                    file.path.contains(DOWNLOADED_FILES_NAME_PATTERN)
                ) {
                    deleteCollectedFile(file)
                }
            }
    }

    private fun updateLastCollectedFileNumber() {
        if (lastCollectedFileNumber < 0) {
            loadSavedFiles(filterTempFiles = true)
                .mapNotNull { file ->
                    file.name
                        .split(SAVED_FILES_NAME_PATTERN)
                        .getOrNull(1)
                        ?.toIntOrNull()
                }.maxOrNull()
                ?.let { lastFileNumber ->
                    lastCollectedFileNumber = lastFileNumber
                }
        }
    }

    @Throws(Exception::class)
    override suspend fun loadCollectedFiles(): List<CollectedFileEntity> {
        return loadSavedFiles(filterTempFiles = true)
    }

    private fun loadSavedFiles(filterTempFiles: Boolean): List<CollectedFileEntity> {
        return if (filesCollectionDir.exists()) {
            filesCollectionDir.listFiles()
                ?.filter { file ->
                    if (filterTempFiles) {
                        !file.name.contains(DOWNLOADED_FILES_NAME_PATTERN)
                    } else true
                }
                ?.map { file ->
                    CollectedFileEntity(
                        name = file.name,
                        path = file.absolutePath,
                        sizeBytes = file.length(),
                        creationDateMillis = file.lastModified()
                    )
                }
                ?: emptyList()
        } else emptyList()
    }

    @Throws(Exception::class)
    override suspend fun renameCollectedFile(newName: String, file: CollectedFileEntity) {
        val localFile = File(file.path)
        if (localFile.exists() && localFile.name != newName) {
            val newFile = File(file.path.substringBeforeLast("/"), newName)
            localFile.renameTo(newFile)
        }
    }

    @Throws(Exception::class)
    override suspend fun deleteCollectedFile(file: CollectedFileEntity) {
        val localFile = File(file.path)
        if (localFile.exists() && !localFile.isDirectory) {
            localFile.delete()
        }
    }

    override fun loadFilesCollectionPath(): String {
        return filesCollectionDir.absolutePath
    }

    override fun loadNewFileNameToCollect(): String {
        return NEW_DOWNLOADED_FILE_NAME.format(System.currentTimeMillis())
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

    private fun List<File>.toFilesEntities(): List<DirEntity> {
        return map { file -> file.toFileEntity() }
    }

    private fun File.toFileEntity(): DirEntity {
        return if (isDirectory) {
            DirEntity.Folder(
                name = name,
                parentPath = parent,
                absolutePath = absolutePath,
                sizeBytes = length(),
                creationDateMillis = lastModified(),
                filesCount = listFiles()?.size ?: 0
            )
        } else {
            DirEntity.File(
                name = name,
                parentPath = parent,
                absolutePath = absolutePath,
                sizeBytes = length(),
                creationDateMillis = lastModified()
            )
        }
    }
}