package com.theeasiestway.data.repositories.files.entities

data class CollectedFileEntity(
    val name: String,
    val path: String,
    val sizeBytes: Long,
    val creationDateMillis: Long
)