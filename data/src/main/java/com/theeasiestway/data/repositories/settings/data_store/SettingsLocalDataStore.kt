package com.theeasiestway.data.repositories.settings.data_store

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.theeasiestway.data.repositories.settings.entities.AppSettingsEntity
import com.theeasiestway.data.repositories.settings.entities.AppThemeEntity
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class SettingsLocalDataStore(
    context: Context
): SettingsDataStore {

    private val context = context.applicationContext

    companion object {
        const val APP_SETTINGS = "APP_SETTINGS"
        const val APP_THEME_KEY = "APP_THEME_KEY"
        const val APP_THEME_VALUE_SYSTEM_DEFAULT = "APP_THEME_VALUE_SYSTEM_DEFAULT"
        const val APP_THEME_VALUE_WHITE = "APP_THEME_VALUE_WHITE"
        const val APP_THEME_VALUE_DARK = "APP_THEME_VALUE_DARK"
        const val COLLECT_DOWNLOADED_MODEL_KEY = "COLLECT_DOWNLOADED_MODEL_KEY"
        const val KEEP_SCREEN_ON_KEY = "KEEP_SCREEN_ON_KEY"
        const val SAVE_LAST_VISITED_FOLDER_KEY = "SAVE_LAST_VISITED_FOLDER_KEY"
        const val LAST_VISITED_FOLDER_KEY = "LAST_VISITED_FOLDER_KEY"
    }

    private val Context.dataStore by preferencesDataStore(name = APP_SETTINGS)
    private val Context.prefsData by lazy {
        context.dataStore.data.catch {
            Log.e("SettingsRepository", "Error while reading value from DataStore: $it")
            emit(emptyPreferences())
        }
    }
    private var cachedAppSettings: AppSettingsEntity? = null

    override suspend fun saveAppSettings(settings: AppSettingsEntity) {
        if (cachedAppSettings?.appTheme != settings.appTheme) {
            saveAppTheme(settings.appTheme)
        }
        if (cachedAppSettings?.collectDownloadedModel != settings.collectDownloadedModel) {
            saveCollectDownloadedModel(settings.collectDownloadedModel)
        }
        if (cachedAppSettings?.keepScreenOn != settings.keepScreenOn) {
            saveKeepScreenOn(settings.keepScreenOn)
        }
        if (cachedAppSettings?.saveLastVisitedFolder != settings.saveLastVisitedFolder) {
            saveLastVisitedFolder(settings.saveLastVisitedFolder)
        }
        cachedAppSettings = settings
    }

    override suspend fun loadAppSettings(): AppSettingsEntity {
        val settings = AppSettingsEntity(
            appTheme = loadAppTheme(),
            collectDownloadedModel = loadCollectDownloadedModel(),
            keepScreenOn = loadKeepScreenOn(),
            saveLastVisitedFolder = loadSaveLastVisitedFolder()
        )
        cachedAppSettings = settings
        return settings
    }

    private suspend fun saveAppTheme(theme: AppThemeEntity) {
        val value = when (theme) {
            AppThemeEntity.SystemDefault -> APP_THEME_VALUE_SYSTEM_DEFAULT
            AppThemeEntity.White -> APP_THEME_VALUE_WHITE
            AppThemeEntity.Dark -> APP_THEME_VALUE_DARK
        }
        putStringValue(APP_THEME_KEY, value)
    }

    private suspend fun loadAppTheme(): AppThemeEntity {
        return when (val value =
            getStringValue(APP_THEME_KEY) ?: APP_THEME_VALUE_SYSTEM_DEFAULT) {
            APP_THEME_VALUE_SYSTEM_DEFAULT -> AppThemeEntity.SystemDefault
            APP_THEME_VALUE_WHITE -> AppThemeEntity.White
            APP_THEME_VALUE_DARK -> AppThemeEntity.Dark
            else -> throw IllegalArgumentException("Unsupported APP_THEME_VALUE value: $value")
        }
    }

    private suspend fun saveCollectDownloadedModel(collectModel: Boolean) {
        putBooleanValue(COLLECT_DOWNLOADED_MODEL_KEY, collectModel)
    }

    private suspend fun loadCollectDownloadedModel(): Boolean {
        return getBooleanValue(COLLECT_DOWNLOADED_MODEL_KEY) ?: true
    }

    private suspend fun saveKeepScreenOn(keepScreenOn: Boolean) {
        putBooleanValue(KEEP_SCREEN_ON_KEY, keepScreenOn)
    }

    private suspend fun loadKeepScreenOn(): Boolean {
        return getBooleanValue(KEEP_SCREEN_ON_KEY) ?: false
    }

    private suspend fun saveLastVisitedFolder(save: Boolean) {
        putBooleanValue(SAVE_LAST_VISITED_FOLDER_KEY, save)
    }

    private suspend fun loadSaveLastVisitedFolder(): Boolean {
        return getBooleanValue(SAVE_LAST_VISITED_FOLDER_KEY) ?: false
    }

    private suspend fun saveLastVisitedFolder(path: String) {
        putStringValue(LAST_VISITED_FOLDER_KEY, path)
    }

    private suspend fun loadLastVisitedFolder(): String? {
        return getStringValue(LAST_VISITED_FOLDER_KEY)
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