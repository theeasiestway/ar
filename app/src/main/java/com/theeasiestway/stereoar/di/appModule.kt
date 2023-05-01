package com.theeasiestway.stereoar.di

import android.text.format.DateFormat
import com.theeasiestway.data.repositories.DownloadsRepositoryImpl
import com.theeasiestway.data.repositories.FilesRepositoryImpl
import com.theeasiestway.data.repositories.SettingsRepositoryImpl
import com.theeasiestway.domain.repositories.DownloadsRepository
import com.theeasiestway.domain.repositories.FilesRepository
import com.theeasiestway.domain.repositories.SettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

const val is24TimeFormatQualifier = "is24TimeFormatQualifier"
const val ioDispatcher = "ioDispatcher"
const val mainDispatcher = "mainDispatcher"
val appModule = module {

    single(named(ioDispatcher)) { Dispatchers.IO }
    single<CoroutineDispatcher>(named(mainDispatcher)) { Dispatchers.Main }

    single(named(is24TimeFormatQualifier)) { DateFormat.is24HourFormat(androidContext()) }

    single<DownloadsRepository> {
        DownloadsRepositoryImpl(
            context = androidContext()
        )
    }

    single<SettingsRepository> {
        SettingsRepositoryImpl(
            context = androidContext(),
            dispatcher = get(named(ioDispatcher))
        )
    }

    single<FilesRepository> {
        FilesRepositoryImpl(
            context = androidContext(),
            downloadsRepository = get(),
            dispatcher = get(named(ioDispatcher))
        )
    }
}