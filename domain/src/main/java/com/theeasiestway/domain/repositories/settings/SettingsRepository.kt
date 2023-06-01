package com.theeasiestway.domain.repositories.settings

import com.theeasiestway.domain.repositories.settings.models.AppSettings

interface SettingsRepository {
    suspend fun saveAppSettings(settings: AppSettings)
    suspend fun getAppSettings(): AppSettings
}