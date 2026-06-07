# Ballast Firebase Crashlytics

## Overview

Extends [Ballast Crash Reporting](./../ballast-crash-reporting) to automatically send ViewModel errors to
[Firebase Crashlytics](https://firebase.google.com/products/crashlytics). Currently only available on Android.

## Supported Platforms

| Platform | Supported |
|----------|-----------|
| JVM      | ❌         |
| Android  | ✅         |
| iOS      | ❌         |
| JS       | ❌         |
| WASM JS  | ❌         |

## See Also

- [Ballast Analytics](./../ballast-analytics)
- [Ballast Firebase Analytics](./../ballast-firebase-analytics)
- [Ballast Crash Reporting](./../ballast-crash-reporting)

## Usage

Add `FirebaseCrashlyticsInterceptor` to your ViewModel configuration. By default, all Inputs that are not annotated
with `@FirebaseCrashlyticsIgnore` will be logged to Crashlytics as breadcrumbs leading up to any recorded exceptions.

```kotlin
class ExampleViewModel(coroutineScope: CoroutineScope) : BasicViewModel<
        ExampleContract.Inputs,
        ExampleContract.Events,
        ExampleContract.State
        >(
    coroutineScope = coroutineScope,
    config = BallastViewModelConfiguration.Builder()
        .withViewModel(ExampleContract.State(), ExampleInputHandler())
        .apply {
            interceptors += FirebaseCrashlyticsInterceptor(
                shouldTrackInput = { input ->
                    when (input) {
                        is ExampleContract.Inputs.SensitiveInput -> false
                        else -> true
                    }
                }
            )
        }
        .build(),
    eventHandler = eventHandler { },
)

object ExampleContract {
    data class State(val loading: Boolean = false)

    sealed interface Inputs {
        data object NormalInput : Inputs

        @FirebaseCrashlyticsIgnore
        data class SensitiveInput(val token: String) : Inputs
    }

    sealed interface Events
}
```

## Installation

```kotlin
repositories {
    mavenCentral()
}

// for plain Android projects
dependencies {
    implementation("io.github.copper-leaf:ballast-firebase-crashlytics:{{ballastVersion}}")
}

// for multiplatform projects
kotlin {
    sourceSets {
        val androidMain by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-firebase-crashlytics:{{ballastVersion}}")
            }
        }
    }
}
```
