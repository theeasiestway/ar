import org.gradle.api.JavaVersion

/**
 * Created by Alexey Loboda on 17.01.2022
 */
object Versions {

    object Main {
        const val gradle = "7.4.0-beta05"
        const val kotlin = "1.8.10"
        const val kotlinKtx = "1.10.0"
        const val kotlinCompilerExt = "1.4.4"
        const val jvmTarget = "1.8"
        val javaVersion = JavaVersion.VERSION_1_8
    }

    object App {
        const val appCompat = "1.6.1"
        const val material = "1.9.0-rc01"

        const val coroutinesX = "1.7.0-RC"
        const val coroutinesAndroid = "1.7.0-RC"
        const val coroutinesViewModelExt = "2.6.1"

        const val composeNavigation = "2.5.3"
        const val composeAccompanist = "0.30.1"
        const val composeActivity = "1.7.1"
        const val composeMaterial = "1.1.0-rc01"
        const val composeAnimation = "1.4.2"
        const val composeUiTooling = "1.4.2"
        const val composeLifecycle = "2.6.1"

        const val dataStore = "1.0.0"

        const val koin = "3.4.0"

        const val mviOrbit = "4.6.1"

        const val kspPlugin = "1.8.10-1.0.9"
        const val composeDestinations = "1.8.41-beta"

        const val filament = "1.8.1"
        const val gltfio = "1.8.1"
        const val arCore = "1.36.0"
    }

    object Test {
        const val junit = "4.13.2"
    }
}