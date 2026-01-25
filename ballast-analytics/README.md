# Ballast Analytics

## Overview

Ballast's Analytics module automatically tracks Inputs sent to your ViewModels to send to your analytics SDK. Support
for Firebase Analytics is supported out-of-the-box on Android via [Ballast Firebase Analytics](./../ballast-firebase-analytics).

## Supported Platforms

| Platform | Supported |
|----------|-----------|
| JVM      | ✅         |
| Android  | ✅         |
| iOS      | ✅         |
| JS       | ✅         |
| WASM JS  | ✅         |

## See Also

- [Ballast Firebase Analytics](./../ballast-firebase-analytics)
- [Ballast Crash Reporting](./../ballast-crash-reporting)
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
            interceptors += AnalyticsInterceptor(
                tracker = ExampleAnalyticsTracker(),

                // implement AnalyticsAdapter for full control over the eventId and eventParameters passed to the Tracker
                adapter = DefaultAnalyticsAdapter(
                    shouldTrackInput = { input ->
                        when (input) {
                            is ExampleContract.Inputs.TrackThis -> true
                            is ExampleContract.Inputs.DontTrackThis -> false
                        }
                    }
                )
            )
        }
        .build(),
    eventHandler = eventHandler { },
)

class ExampleAnalyticsTracker : AnalyticsTracker {
    override fun trackAnalyticsEvent(
        eventId: String,
        eventParameters: Map<String, String>
    ) {
        // TODO: track this event to your analytics SDK
    }
}
```

[Source](./src/commonTest/kotlin/com/copperleaf/ballast/analytics/vm/TestViewModel.kt)

## Installation

```kotlin
repositories {
    mavenCentral()
}

// for plain JVM or Android projects
dependencies {
    implementation("io.github.copper-leaf:ballast-analytics:{{ballastVersion}}")
}

// for multiplatform projects
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-analytics:{{ballastVersion}}")
            }
        }
    }
}
```
