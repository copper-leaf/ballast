# Ballast Firebase Analytics

## Overview

This module extends the capabilities of [Ballast Analytics](./../ballast-analytics/README.md) to send analytics to
[Firebase Analytics](https://firebase.google.com/products/analytics). Currently only available on Android.

## Supported Platforms

| Platform | Supported |
|----------|-----------|
| JVM      | ❌         |
| Android  | ✅         |
| iOS      | ❌         |
| JS       | ❌         |
| WASM JS  | ❌         |

## See Also

- [Ballast Analytics](./../ballast-analytics/README.md)
- [Ballast Crash Reporting](./../ballast-crash-reporting/README.md)
- [Ballast Firebase Crashlytics](./../ballast-firebase-crashlytics/README.md)

## Usage

Add the `FirebaseAnalyticsInterceptor` to your ViewModel configuration to track inputs and send them to Firebase 
Analytics automatically. Only Inputs annotated with `@FirebaseAnalyticsTrackInput` will be tracked. Make sure any inputs
annotated with @FirebaseAnalyticsTrackInput do not leak any sensitive information through their `.toString()` value.

```kotlin
class TestViewModel(coroutineScope: CoroutineScope) : BasicViewModel<
        TestContract.Inputs,
        TestContract.Events,
        TestContract.State
        >(
    coroutineScope = coroutineScope,
    config = BallastViewModelConfiguration.Builder()
        .withViewModel(TestContract.State(), TestInputHandler())
        .apply {
            interceptors += FirebaseAnalyticsInterceptor()
        }
        .build(),
    eventHandler = eventHandler { },
)

object TestContract {
    data class State(
        val loading: Boolean = false,
    )

    sealed interface Inputs {

        @FirebaseAnalyticsTrackInput
        data object TrackThis : Inputs

        data object DontTrackThis : Inputs
    }

    sealed interface Events
}
```

## Installation

```kotlin
repositories {
    mavenCentral()
}

// for plain JVM or Android projects
dependencies {
    implementation("io.github.copper-leaf:ballast-firebase-analytics:{{ballastVersion}}")
}

// for multiplatform projects
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-firebase-analytics:{{ballastVersion}}")
            }
        }
    }
}
```
