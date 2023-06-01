package com.theeasiestway.data.repositories.files.entities

data class FilesTreeEntity(
    val rootPath: String,
    val internalStorageRootPath: String,
    val externalStorageRootPath: String?,
    val currentPath: String,
    val parentPath: String?,
    val files: List<DirEntity>
)