package com.theeasiestway.domain.model

data class CollectedModel(
    val name: String,
    val path: String,
    val sizeBytes: Long,
    val creationDateMillis: Long
)