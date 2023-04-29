package com.theeasiestway.stereoar.app

import android.app.Application
import com.theeasiestway.stereoar.di.appModule
import com.theeasiestway.stereoar.di.modelViewModule
import com.theeasiestway.stereoar.di.modelsExplorerModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

/**
 * Created by Alexey Loboda on 17.01.2022
 */
class App: Application() {
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