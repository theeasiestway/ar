package com.theeasiestway.data.repositories.settings.entities

import com.theeasiestway.domain.repositories.settings.models.AppSettings
import com.theeasiestway.domain.repositories.settings.models.AppTheme

fun AppThemeEntity.toDomain(): AppTheme {
    return when(this) {
        AppThemeEntity.SystemDefault -> AppTheme.SystemDefault
        AppThemeEntity.White -> AppTheme.White
        AppThemeEntity.Dark -> AppTheme.Dark
    }
}

fun AppTheme.toEntity(): AppThemeEntity {
    return when(this) {
        AppTheme.SystemDefault -> AppThemeEntity.SystemDefault
        AppTheme.White -> AppThemeEntity.White
        AppTheme.Dark -> AppThemeEntity.Dark
    }
}

fun AppSettingsEntity.toDomain(): AppSettings {
    return AppSettings(
        appTheme = appTheme.toDomain(),
        collectDownloadedModel = collectDownloadedModel,
        keepScreenOn = keepScreenOn,
        saveLastVisitedFolder = saveLastVisitedFolder
    )
}

fun AppSettings.toEntity(): AppSettingsEntity {
    return AppSettingsEntity(
        appTheme = appTheme.toEntity(),
        collectDownloadedModel = collectDownloadedModel,
        keepScreenOn = keepScreenOn,
        saveLastVisitedFolder = saveLastVisitedFolder
    )
}