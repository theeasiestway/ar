package com.theeasiestway.stereoar.di

import android.text.format.DateFormat
import com.theeasiestway.data.repositories.FilesRepositoryImpl
import com.theeasiestway.data.repositories.SettingsRepositoryImpl
import com.theeasiestway.domain.repositories.FilesRepository
import com.theeasiestway.domain.repositories.SettingsRepository
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

const val is24TimeFormatQualifier = "is24TimeFormatQualifier"
val appModule = module {

    single { Dispatchers.IO }

    single(named(is24TimeFormatQualifier)) { DateFormat.is24HourFormat(androidContext()) }
}