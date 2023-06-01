package com.theeasiestway.data.repositories.settings.data_store

import com.theeasiestway.data.repositories.settings.entities.AppSettingsEntity

interface SettingsDataStore {
    suspend fun saveAppSettings(settings: AppSettingsEntity)
    suspend fun loadAppSettings(): AppSettingsEntity
}