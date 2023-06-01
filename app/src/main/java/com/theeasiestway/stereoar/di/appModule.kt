package com.theeasiestway.stereoar.di

import android.text.format.DateFormat
import com.theeasiestway.data.repositories.downloads.DownloadsRepositoryImpl
import com.theeasiestway.data.repositories.downloads.data_store.InternetDownloadsDataStore
import com.theeasiestway.data.repositories.files.FilesRepositoryImpl
import com.theeasiestway.data.repositories.files.data_store.FilesLocalDataStore
import com.theeasiestway.data.repositories.settings.SettingsRepositoryImpl
import com.theeasiestway.data.repositories.settings.data_store.SettingsLocalDataStore
import com.theeasiestway.domain.repositories.downloads.DownloadsRepository
import com.theeasiestway.domain.repositories.files.FilesRepository
import com.theeasiestway.domain.repositories.settings.SettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

const val is24TimeFormatQualifier = "is24TimeFormatQualifier"
const val ioDispatcher = "ioDispatcher"
const val mainDispatcher = "mainDispatcher"
const val appScope = "appScope"
val appModule = module {

    single(named(ioDispatcher)) { Dispatchers.IO }
    single<CoroutineDispatcher>(named(mainDispatcher)) { Dispatchers.Main }
    single(named(appScope)) { androidApplication() as CoroutineScope }

    single(named(is24TimeFormatQualifier)) { DateFormat.is24HourFormat(androidContext()) }

    single<DownloadsRepository> {
        DownloadsRepositoryImpl(
            dataStore = InternetDownloadsDataStore(androidContext()),
            ioDispatcher = get(named(ioDispatcher))
        )
    }

    single<SettingsRepository> {
        SettingsRepositoryImpl(
            dataStore = SettingsLocalDataStore(androidContext()),
            ioDispatcher = get(named(ioDispatcher))
        )
    }

    single<FilesRepository> {
        FilesRepositoryImpl(
            dataStore = FilesLocalDataStore(androidContext()),
            ioDispatcher = get(named(ioDispatcher))
        )
    }
}