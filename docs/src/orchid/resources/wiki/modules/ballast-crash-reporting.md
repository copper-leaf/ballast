---
---

# {{ page.title }}

## Overview

Ballast's Crash Reporting module automatically sends errors in your ViewModels to you crash reporting SDK. Support for 
Firebase Crashlytics is supported out-of-the-box on Android.

Since v3.0.0, crash reporting is now available in all targets, for use with other analytics trackers.

## Usage

Ballast's Crashlytics integration provides automatic tracing of your Inputs and gives you Logs and Keys attached to your
crash reports to aid in identifying and getting to the root cause of your application issues. Crashlytics should be 
integrated in your app [as normal][1], and then 
you need to add the [`ballast-firebase-crashlytics`](#Installation) dependency, and add the Interceptor to your ViewModel 
configuration. Note that the below example uses `AndroidViewModel`, but the `FirebaseCrashlyticsInterceptor` will work 
just the same with any other Ballast ViewModel type (Repositories, BasicViewModel, etc.).

```kotlin
@HiltViewModel
class ExampleViewModel
@Inject
constructor() : AndroidViewModel<
        ExampleContract.Inputs,
        ExampleContract.Events,
        ExampleContract.State>(
    config = BallastViewModelConfiguration.Builder()
        .apply {
            // customized interceptor
            this += CrashReportingInterceptor(
                tracker = FirebaseCrashReporter(Firebase.crashlytics),
                shouldTrackInput = { !it.isAnnotatedWith<FirebaseCrashlyticsIgnore>() },
            )
            // helper function for setting up crash reporting with Firebase
            this += FirebaseCrashlyticsInterceptor() // FirebaseCrashlyticsInterceptor factory function, which returns CrashReportingInterceptor
        }
        .withViewModel(
            initialState = ExampleContract.State(),
            inputHandler = ExampleInputHandler(),
            name = "Example",
        )
        .build()
)
```

Once installed, the Firebase Crashlytics integration will automatically start logging all Inputs to the 
[Firebase Crashlytics Logger][2]. 

However, it's likely that you don't actually want all Inputs sent to Firebase, especially for things like updating text, 
because they will spam the logs and hide the actual important steps the user had taken which led up to the error. 
Alternatively, you will probably have some inputs that contain sensitive information (passwords, API keys, PII, etc.) 
that also should not be set to Firebase. By annotating any Input with `FirebaseCrashlyticsIgnore`, it will not be sent 
in the crash logs. Each Input will be logged using its `.toString()` value, so be sure to override `.toString()` for any
inputs you do want tracked to remove any sensitive info from them.

{% alert 'warning' :: compileAs('md') %}
**Warning**

Add `@FirebaseCrashlyticsIgnore` to Inputs you do not want to sent to Firebase, to protect sensitive information.
{% endalert %}

In addition to logs, the `FirebaseCrashlyticsInterceptor` will also record any exceptions that are thrown but do not 
crash the app as a [non-fatal exception][3]

## Installation

```kotlin
repositories {
    mavenCentral()
}

// for plain JVM or Android projects
dependencies {
    implementation("io.github.copper-leaf:ballast-crash-reporting:{{site.version}}")
    implementation("io.github.copper-leaf:ballast-firebase-crashlytics:{{site.version}}")
}

// for multiplatform projects
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-crash-reporting:{{site.version}}")
                implementation("io.github.copper-leaf:ballast-firebase-crashlytics:{{site.version}}")
            }
        }
    }
}
```

[1]: https://firebase.google.com/docs/crashlytics/get-started?platform=android
[2]: https://firebase.google.com/docs/crashlytics/customize-crash-reports?platform=android#add-logs
[3]: https://firebase.google.com/docs/crashlytics/customize-crash-reports?platform=android#log-excepts
[4]: https://firebase.google.com/docs/analytics/get-started?platform=android
