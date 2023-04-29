package com.theeasiestway.domain.repositories

interface SettingsRepository {
    suspend fun saveLastVisitedFolder(path: String)
    suspend fun loadLastVisitedFolder(): String?
}