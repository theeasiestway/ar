package com.theeasiestway.stereoar.app

import android.app.Application
import android.util.Log
import com.theeasiestway.stereoar.di.appModule
import com.theeasiestway.stereoar.di.ioDispatcher
import com.theeasiestway.stereoar.di.modelViewModule
import com.theeasiestway.stereoar.di.modelsExplorerModule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import kotlin.coroutines.CoroutineContext

/**
 * Created by Alexey Loboda on 17.01.2022
 */
class App: Application(), CoroutineScope {

    override val coroutineContext: CoroutineContext by lazy {
        SupervisorJob() +
                get(named(ioDispatcher)) as CoroutineDispatcher +
                CoroutineExceptionHandler { _, error -> Log.e("AppCoroutineScope", "error: $error") }
    }
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            androidLogger()
            modules(
                appModule,
                modelsExplorerModule,
                modelViewModule
            )
        }
    }
}