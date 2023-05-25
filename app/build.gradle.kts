@file:Suppress("UnstableApiUsage")
plugins {
    kotlin(AppDeps.Plugins.Kotlin.android)
    id(AppDeps.Plugins.Id.application)
    id(AppDeps.Plugins.Id.parcelize)
    id(AppDeps.Plugins.Id.ksp) version Versions.App.kspPlugin
}

android {
    compileSdk = AppConfigs.compileSdkVersion
    defaultConfig {
        applicationId = AppConfigs.applicationId
        minSdk = AppConfigs.minSdkVersion
        targetSdk = AppConfigs.targetSdkVersion
        versionCode = AppConfigs.versionCode
        versionName = AppConfigs.versionName
    }
    buildFeatures {
        compose = AppConfigs.composeEnabled
    }
    compileOptions {
        sourceCompatibility = Versions.Main.javaVersion
        targetCompatibility = Versions.Main.javaVersion
    }
    composeOptions {
        kotlinCompilerExtensionVersion = Versions.Main.kotlinCompilerExt
    }
    kotlinOptions {
        jvmTarget = Versions.Main.jvmTarget
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = AppConfigs.minifyEnabled
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
    namespace = AppConfigs.appNamespace
}

dependencies {
    implementation(project(":data"))
    implementation(AppDeps.Main.kotlinStdlib)
    implementation(AppDeps.App.appCompat)
    implementation(AppDeps.App.coroutinesViewModelExt)
    implementation(AppDeps.App.composeNavigation)
    implementation(AppDeps.App.composePermissions)
    implementation(AppDeps.App.composeActivity)
    implementation(AppDeps.App.composeMaterial)
    implementation(AppDeps.App.composeViewPager)
    implementation(AppDeps.App.composeConstraintLayout)
    implementation(AppDeps.App.composeAnimation)
    implementation(AppDeps.App.composeUiTooling)
    implementation(AppDeps.App.composeLifecycle)
    implementation(AppDeps.App.koinAndroid)
    implementation(AppDeps.App.koinCompose)
    implementation(AppDeps.App.mviOrbitCore)
    implementation(AppDeps.App.mviOrbitViewModel)
    implementation(AppDeps.App.mviOrbitCompose)
    implementation(AppDeps.App.composeDestinationsCore)
    implementation(AppDeps.App.composeDestinationsAnimations)
    ksp(AppDeps.App.composeDestinationsKsp)
}