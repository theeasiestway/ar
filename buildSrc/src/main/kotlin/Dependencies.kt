/**
 * Created by Alexey Loboda on 17.01.2022
 */

object AppConfigs {
    const val applicationId = "com.theeasiestway.stereoar"
    const val appNamespace = applicationId
    const val dataNamespace = "com.theeasiestway.data"
    const val sceneformNamespace = "com.google.ar.sceneform"
    const val sceneformUxNamespace = "com.google.ar.sceneform.ux"
    const val compileSdkVersion = 33
    const val minSdkVersion = 24 // Sceneform requires minSdkVersion >= 24.
    const val targetSdkVersion = compileSdkVersion
    const val versionCode = 1
    const val versionName =  "1.0"
    const val composeEnabled = true
    const val minifyEnabled = false
}

object AppDeps {

    object Plugins {
        object Kotlin {
            val android = "android"
        }
        object Id {
            val application = "com.android.application"
            val parcelize = "kotlin-parcelize"
            val ksp = "com.google.devtools.ksp"
        }
    }

    object Main {
        val gradlePlugin = "com.android.tools.build:gradle:${Versions.Main.gradle}"
        val kotlinPlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.Main.kotlin}"
        val kotlinAndroidKtx = "androidx.core:core-ktx:${Versions.Main.kotlinKtx}"
        val kotlinAndroidExt = "org.jetbrains.kotlin:kotlin-android-extensions:${Versions.Main.kotlin}"
        val kotlinStdlib = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.Main.kotlin}"
    }

    object App {
        val appCompat = "androidx.appcompat:appcompat:${Versions.App.appCompat}"
        val androidMaterial = "com.google.android.material:material:${Versions.App.material}"

        val coroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.App.coroutinesAndroid}"
        val coroutinesViewModelExt = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.App.coroutinesViewModelExt}"

        val composeNavigation = "androidx.navigation:navigation-compose:${Versions.App.composeNavigation}"
        val composePermissions = "com.google.accompanist:accompanist-permissions:${Versions.App.composeAccompanist}"
        val composeViewPager = "com.google.accompanist:accompanist-pager:${Versions.App.composeAccompanist}"
        val composeActivity = "androidx.activity:activity-compose:${Versions.App.composeActivity}"
        val composeMaterial = "androidx.compose.material3:material3:${Versions.App.composeMaterial}"
        val composeAnimation = "androidx.compose.animation:animation:${Versions.App.composeAnimation}"
        val composeUiTooling = "androidx.compose.ui:ui-tooling:${Versions.App.composeUiTooling}"
        val composeLifecycle = "androidx.lifecycle:lifecycle-runtime-compose:${Versions.App.composeLifecycle}"

        val dataStore = "androidx.datastore:datastore-preferences:${Versions.App.dataStore}"

        val koinAndroid = "io.insert-koin:koin-android:${Versions.App.koin}"
        val koinCompose = "io.insert-koin:koin-androidx-compose:${Versions.App.koin}"

        val mviOrbitCore = "org.orbit-mvi:orbit-core:${Versions.App.mviOrbit}"
        val mviOrbitViewModel = "org.orbit-mvi:orbit-viewmodel:${Versions.App.mviOrbit}"
        val mviOrbitCompose = "org.orbit-mvi:orbit-compose:${Versions.App.mviOrbit}"

        val composeDestinationsCore = "io.github.raamcosta.compose-destinations:core:${Versions.App.composeDestinations}"
        val composeDestinationsKsp = "io.github.raamcosta.compose-destinations:ksp:${Versions.App.composeDestinations}"
        val composeDestinationsAnimations = "io.github.raamcosta.compose-destinations:animations-core:${Versions.App.composeDestinations}"

        val filamentAndroid = "com.google.android.filament:filament-android:${Versions.App.filament}"
        val gltfio = "com.google.android.filament:gltfio-android:${Versions.App.gltfio}"
        val arCore = "com.google.ar:core:${Versions.App.arCore}"
    }

    object Test {
        val junit = "junit:junit:${Versions.Test.junit}"
    }
}