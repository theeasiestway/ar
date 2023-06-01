package com.theeasiestway.data.repositories.settings

import com.theeasiestway.data.repositories.settings.data_store.SettingsDataStore
import com.theeasiestway.data.repositories.settings.entities.toDomain
import com.theeasiestway.data.repositories.settings.entities.toEntity
import com.theeasiestway.domain.repositories.settings.SettingsRepository
import com.theeasiestway.domain.repositories.settings.models.AppSettings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class SettingsRepositoryImpl(
    private val dataStore: SettingsDataStore,
    private val ioDispatcher: CoroutineDispatcher
): SettingsRepository {

    override suspend fun saveAppSettings(settings: AppSettings) {
        withContext(ioDispatcher) {
            dataStore.saveAppSettings(settings.toEntity())
        }
    }

    override suspend fun getAppSettings(): AppSettings {
        return withContext(ioDispatcher) {
            dataStore.loadAppSettings().toDomain()
        }
    }
}