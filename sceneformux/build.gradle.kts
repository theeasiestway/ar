plugins {
    id("com.android.library")
}

android {
    compileSdk = AppConfigs.compileSdkVersion
    defaultConfig {
        minSdk = AppConfigs.minSdkVersion
    }
    compileOptions {
        sourceCompatibility = Versions.Main.javaVersion
        targetCompatibility = Versions.Main.javaVersion
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = AppConfigs.minifyEnabled
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
    namespace = AppConfigs.sceneformUxNamespace
}

dependencies {
    // Use the Sceneform SDK built from the source files included in the sceneformsrc folder.
    api(project(":sceneform"))
    implementation(AppDeps.App.appCompat)
    implementation(AppDeps.App.androidMaterial)
}