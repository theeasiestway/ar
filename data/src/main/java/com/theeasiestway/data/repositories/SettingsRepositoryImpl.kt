package com.theeasiestway.data.repositories

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.theeasiestway.domain.repositories.SettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SettingsRepositoryImpl(
    context: Context,
    private val dispatcher: CoroutineDispatcher
): SettingsRepository {

    private val context = context.applicationContext

    companion object {
        const val APP_SETTINGS = "APP_SETTINGS"
        const val LAST_VISITED_FOLDER_KEY = "LAST_VISITED_FOLDER_KEY"
    }

    private val Context.dataStore by preferencesDataStore(name = APP_SETTINGS)
    private val Context.prefsData by lazy {
        context.dataStore.data.catch {
            Log.e("SettingsRepository", "Error while reading value from DataStore: $it")
            emit(emptyPreferences())
        }
    }

    override suspend fun saveLastVisitedFolder(path: String) {
        withContext(dispatcher) {
            putStringValue(LAST_VISITED_FOLDER_KEY, path)
        }
    }

    override suspend fun loadLastVisitedFolder(): String? {
        return withContext(dispatcher) {
            getStringValue(LAST_VISITED_FOLDER_KEY)
        }
    }

    private suspend fun putStringValue(key: String, value: String) {
        context.dataStore.edit { prefs -> prefs[stringPreferencesKey(key)] = value }
    }

    private suspend fun getStringValue(key: String): String? {
        return context.prefsData
            .map { prefs -> prefs[stringPreferencesKey(key)] }
            .firstOrNull()
    }

    private suspend fun putBooleanValue(key: String, value: Boolean) {
        context.dataStore.edit { prefs -> prefs[booleanPreferencesKey(key)] = value }
    }

    private suspend fun getBooleanValue(key: String): Boolean? {
        return context.prefsData
            .map { prefs -> prefs[booleanPreferencesKey(key)] }
            .firstOrNull()
    }
}