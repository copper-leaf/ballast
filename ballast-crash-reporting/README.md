# Ballast Crash Reporting

## Overview

Ballast's Crash Reporting module automatically sends errors in your ViewModels to you crash reporting SDK. Support
for Firebase Crashlytics is supported out-of-the-box on Android via [Ballast Firebase Crashlytics](./../ballast-firebase-crashlytics).

## Supported Platforms

| Platform | Supported |
|----------|-----------|
| JVM      | ✅         |
| Android  | ✅         |
| iOS      | ✅         |
| JS       | ✅         |
| WASM JS  | ✅         |

## See Also

- [Ballast Analytics](./../ballast-analytics)
- [Ballast Firebase Analytics](./../ballast-firebase-analytics)
- [Ballast Firebase Crashlytics](./../ballast-firebase-crashlytics)

## Usage

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
            interceptors += CrashReportingInterceptor(
                crashReporter = ExampleCrashReporter(),
                shouldTrackInput = { input ->
                    when (input) {
                        is ExampleContract.Inputs.TrackThis -> true
                        is ExampleContract.Inputs.DontTrackThis -> false
                    }
                }
            )
        }
        .build(),
    eventHandler = eventHandler { },
)

class ExampleCrashReporter : CrashReporter {
    override fun logInput(viewModelName: String, input: Any) {
        // log the event to your crash reporting system for trace of steps leading to a crash. Only inputs returning
        // true from `shouldTrackInput` are sent here.
    }

    override fun recordInputError(viewModelName: String, input: Any, throwable: Throwable) {
        // record the error caused when handling an Input
    }

    override fun recordEventError(viewModelName: String, event: Any, throwable: Throwable) {
        // record the error caused when handling an Input
    }

    override fun recordSideJobError(viewModelName: String, key: String, throwable: Throwable) {
        // record the error caused by a running SideJob
    }

    override fun recordUnhandledError(viewModelName: String, throwable: Throwable) {
        // record the error caused by something else (most likely out of your control)
    }
}
```

## Installation

```kotlin
repositories {
    mavenCentral()
}

// for plain JVM or Android projects
dependencies {
    implementation("io.github.copper-leaf:ballast-crash-reporting:{{ballastVersion}}")
}

// for multiplatform projects
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-crash-reporting:{{ballastVersion}}")
            }
        }
    }
}
```
