package com.theeasiestway.domain.model

sealed interface FileUri {
    val uri: String

    data class File(override val uri: String): FileUri
    data class Url(override val uri: String): FileUri
}