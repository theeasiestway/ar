plugins {
    id(AppDeps.Plugins.Id.javaLibrary)
    kotlin(AppDeps.Plugins.Kotlin.jvm)
}

java {
    sourceCompatibility = Versions.Main.javaVersion
    targetCompatibility = Versions.Main.javaVersion
}

dependencies {
    api(AppDeps.App.coroutinesX)
}