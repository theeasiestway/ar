package com.theeasiestway.stereoar.di

import android.text.format.DateFormat
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

const val is24TimeFormatQualifier = "is24TimeFormatQualifier"
const val ioDispatcher = "ioDispatcher"
val appModule = module {

    single(named(ioDispatcher)) { Dispatchers.IO }

    single(named(is24TimeFormatQualifier)) { DateFormat.is24HourFormat(androidContext()) }
}