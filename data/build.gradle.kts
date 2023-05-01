plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = AppConfigs.compileSdkVersion

    defaultConfig {
        minSdk = AppConfigs.minSdkVersion
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = AppConfigs.minifyEnabled
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = Versions.Main.javaVersion
        targetCompatibility = Versions.Main.javaVersion
    }
    namespace = AppConfigs.dataNamespace
}

dependencies {
    // Use the Sceneform UX Package built from the source files included in the sceneformux folder.
    api(project(":sceneformux"))
    api(project(":domain"))
    implementation(AppDeps.Main.kotlinAndroidKtx)
    implementation(AppDeps.App.dataStore)
    testImplementation(AppDeps.Test.junit)
}