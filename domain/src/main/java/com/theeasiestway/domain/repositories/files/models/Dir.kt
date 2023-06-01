package com.theeasiestway.domain.repositories.files.models

sealed interface Dir {
    val name: String
    val parentPath: String?
    val absolutePath: String
    val sizeBytes: Long
    val creationDateMillis: Long

    data class File(
        override val name: String,
        override val parentPath: String?,
        override val absolutePath: String,
        override val sizeBytes: Long,
        override val creationDateMillis: Long,
    ): Dir

    data class Folder(
        override val name: String,
        override val parentPath: String?,
        override val absolutePath: String,
        override val sizeBytes: Long,
        override val creationDateMillis: Long,
        val filesCount: Int
    ): Dir
}