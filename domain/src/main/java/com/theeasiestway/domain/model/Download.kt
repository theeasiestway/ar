package com.theeasiestway.domain.model

data class Download(
    val id: Long,
    val status: Result<Unit>
)