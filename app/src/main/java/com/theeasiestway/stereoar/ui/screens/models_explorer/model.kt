package com.theeasiestway.stereoar.ui.screens.models_explorer

import com.theeasiestway.domain.model.CollectedModel

sealed interface DisplayablePath {
    object Root: DisplayablePath
    data class InternalStorage(val path: String): DisplayablePath
    data class ExternalStorage(val path: String): DisplayablePath
}

sealed class PagerPage {
    data class FilesExplorer(
        val displayablePath: DisplayablePath,
        val currentPath: String,
        val files: List<FileItem>,
        val canMoveBack: Boolean
    ): PagerPage()

    data class ModelsCollection(
        val models: List<CollectedModel>
    ): PagerPage()
}

sealed interface FileItem {
    val parentPath: String?
    val path: String

    data class Root(
        override val parentPath: String?,
        override val path: String,
        val isExternalStorage: Boolean
    ): FileItem

    sealed interface NotRoot: FileItem {
        val title: String
        val creationDateMillis: Long

        data class Folder(
            override val parentPath: String?,
            override val path: String,
            override val title: String,
            override val creationDateMillis: Long,
            val filesCount: Int
        ): NotRoot

        data class File(
            override val parentPath: String?,
            override val path: String,
            override val title: String,
            override val creationDateMillis: Long,
            val sizeBytes: Long
        ): NotRoot {
            fun isArModel(): Boolean {
                return title.endsWith(".glb", ignoreCase = true) || title.endsWith(".gltf", ignoreCase = true)
            }
        }
    }
}