package com.theeasiestway.stereoar.ui.screens.choose_model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/**
 * Created by Alexey Loboda on 17.07.2022
 */

data class ModelItem(
    val path: String,
    val title: String,
    val size: String,
    val creationDate: String
)

sealed class PagerPage {
    abstract val title: Int

    data class FilesExplorerState(
        @StringRes override val title: Int,
        val displayablePath: String,
        val currentPath: String,
        val files: List<FileItem>,
        val canMoveBack: Boolean
    ): PagerPage()

    data class FavoritesModelsState(
        @StringRes override val title: Int,
        val models: List<ModelItem>
    ): PagerPage()
}

sealed class FileItem {
    abstract val parentPath: String?
    abstract val path: String
    abstract val title: String
    abstract val icon: Int
    abstract val creationDate: String?

    data class Folder(
        override val parentPath: String?,
        override val path: String,
        override val title: String,
        @DrawableRes override val icon: Int,
        override val creationDate: String?,
        val itemsCount: String?
    ): FileItem()

    data class File(
        override val parentPath: String?,
        override val path: String,
        override val title: String,
        @DrawableRes override val icon: Int,
        override val creationDate: String,
        val size: String,
    ): FileItem()
}