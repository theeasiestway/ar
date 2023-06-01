package com.theeasiestway.domain.repositories.files.models

data class CollectedModel(
    val name: String,
    val path: String,
    val sizeBytes: Long,
    val creationDateMillis: Long
)