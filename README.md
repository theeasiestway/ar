#### 3D models viewer that allows you to view .glb models in augmented reality using the camera of the phone.

#### The work is still in progress but currently the app consists of two main screens:
* Files Explorer:
    * Allows to find and choose .glb model on the internal or external storage of the phone for viewing.
    * Allows to download .glb model from a URL for viewing.
    * Allows to manage the models that were added to models collection.
* Model Viewer:
    * Allows to view .glb model that was chosen on files explorer screen or downloaded from a URL in augmented reality using the camera of the phone.
    * Allows to scale, rotate and move the model.
    * Allows to save the model to models collection.


#### Used technogies and libraries:
* Gradle dependency management by [buildSrc](https://medium.com/codex/clean-dependency-management-in-multi-module-android-projects-49f2a0df8d2f) and [Kotlin DSL](https://docs.gradle.org/current/userguide/kotlin_dsl.html)
* Multithreading by [Kotlin Coroutines](https://github.com/Kotlin/kotlinx.coroutines)
* Clean Architecture by my hands
* DI by [Koin](https://github.com/InsertKoinIO/koin)
* MVI by [OrbitMVI](https://github.com/orbit-mvi/orbit-mvi) and [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel)
* Navigation by [Compose Destinations](https://github.com/raamcosta/compose-destinations)
* Settings caching by [DataStore](https://developer.android.com/jetpack/androidx/releases/datastore)
* UI by [Jetpack Compose](https://developer.android.com/jetpack/compose)
* Android permissions by [Accompanist](https://github.com/google/accompanist)
* Augmented Reality by [ARCore](https://github.com/google-ar/arcore-android-sdk)

