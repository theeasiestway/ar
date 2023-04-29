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
    namespace = AppConfigs.sceneformNamespace
}

dependencies {
    api(AppDeps.App.filamentAndroid)
    api(AppDeps.App.gltfio)
    api(AppDeps.App.arCore)
    implementation(files("../sceneform/libs/libsceneform_runtime_schemas.jar"))
    implementation(AppDeps.App.appCompat)
    implementation(AppDeps.App.androidMaterial)
}