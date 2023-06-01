package com.theeasiestway.domain.repositories.files.models

interface Uri {
    val uri: String
}
data class FileUri(override val uri: String): Uri
data class UrlUri(override val uri: String): Uri