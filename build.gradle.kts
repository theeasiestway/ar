buildscript {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
    dependencies {
        classpath(AppDeps.Main.gradlePlugin)
        classpath(AppDeps.Main.kotlinPlugin)
        classpath(AppDeps.Main.kotlinAndroidExt)
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}