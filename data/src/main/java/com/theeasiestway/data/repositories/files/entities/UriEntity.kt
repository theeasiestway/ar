package com.theeasiestway.data.repositories.files.entities

interface UriEntity {
    val uri: String
}

data class FileUriEntity(override val uri: String) : UriEntity
data class UrlUriEntity(override val uri: String) : UriEntity