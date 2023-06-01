package com.theeasiestway.domain.repositories.files.models


data class FilesTree(
    val rootPath: String,
    val internalStorageRootPath: String,
    val externalStorageRootPath: String?,
    val currentPath: String,
    val parentPath: String?,
    val files: List<Dir>
)