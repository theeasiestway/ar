package com.theeasiestway.stereoar.ui.screens.models_explorer

import com.theeasiestway.domain.model.FileUri

fun ModelUri.toFileUri(): FileUri {
    return when(this) {
        is ModelUri.File -> toFileUri()
        is ModelUri.Url -> toFileUri()
    }
}

fun ModelUri.File.toFileUri(): FileUri.File {
    return FileUri.File(uri = uri)
}

fun ModelUri.Url.toFileUri(): FileUri.Url {
    return FileUri.Url(uri = uri)
}