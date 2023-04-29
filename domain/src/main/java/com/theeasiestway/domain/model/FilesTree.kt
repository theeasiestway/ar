package com.theeasiestway.domain.model

import java.io.File

data class FilesTree(
    val rootPath: String,
    val internalStorageRootPath: String,
    val externalStorageRootPath: String?,
    val currentPath: String,
    val parentPath: String?,
    val files: List<File>
)