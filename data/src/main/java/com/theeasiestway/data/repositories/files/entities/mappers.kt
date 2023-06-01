package com.theeasiestway.data.repositories.files.entities

import com.theeasiestway.domain.repositories.files.models.CollectedModel
import com.theeasiestway.domain.repositories.files.models.Dir
import com.theeasiestway.domain.repositories.files.models.FileUri
import com.theeasiestway.domain.repositories.files.models.FilesTree
import com.theeasiestway.domain.repositories.files.models.Uri
import com.theeasiestway.domain.repositories.files.models.UrlUri

fun CollectedFileEntity.toDomain(): CollectedModel {
    return CollectedModel(
        name = name,
        path = path,
        sizeBytes = sizeBytes,
        creationDateMillis = creationDateMillis
    )
}

fun CollectedModel.toEntity(): CollectedFileEntity {
    return CollectedFileEntity(
        name = name,
        path = path,
        sizeBytes = sizeBytes,
        creationDateMillis = creationDateMillis
    )
}

@JvmName("filesEntitiesToDomain")
fun List<CollectedFileEntity>.toDomain(): List<CollectedModel> {
    return map { file -> file.toDomain() }
}

fun DirEntity.toDomain(): Dir {
    return when(this) {
        is DirEntity.File -> Dir.File(
            name = name,
            parentPath = parentPath,
            absolutePath = absolutePath,
            sizeBytes = sizeBytes,
            creationDateMillis = creationDateMillis
        )
        is DirEntity.Folder -> Dir.Folder(
            name = name,
            parentPath = parentPath,
            absolutePath = absolutePath,
            sizeBytes = sizeBytes,
            creationDateMillis = creationDateMillis,
            filesCount = filesCount
        )
    }
}

@JvmName("dirEntitiesToDomain")
fun List<DirEntity>.toDomain(): List<Dir> {
    return map { dir -> dir.toDomain() }
}

fun FilesTreeEntity.toDomain(): FilesTree {
    return FilesTree(
        rootPath = rootPath,
        internalStorageRootPath = internalStorageRootPath,
        externalStorageRootPath = externalStorageRootPath,
        currentPath = currentPath,
        parentPath = parentPath,
        files = files.toDomain()
    )
}

fun UriEntity.toDomain(): Uri {
    return when(this) {
        is FileUriEntity -> FileUri(uri)
        is UrlUriEntity -> UrlUri(uri)
        else -> error("Unsupported type of UriEntity: $this")
    }
}

fun FileUri.toEntity(): FileUriEntity {
    return FileUriEntity(uri)
}