package com.theeasiestway.stereoar.ui.screens.models_explorer

fun FileUri.toDomain(): com.theeasiestway.domain.repositories.files.models.FileUri {
    return com.theeasiestway.domain.repositories.files.models.FileUri(uri = uri)
}

fun UrlUri.toDomain(): com.theeasiestway.domain.repositories.files.models.UrlUri {
    return com.theeasiestway.domain.repositories.files.models.UrlUri(uri = uri)
}