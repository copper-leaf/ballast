---
---

# {{ page.title }}

## Overview

Ballast's Analytics module automatically tracks Inputs sent to your ViewModels to send to your analytics SDK. Support 
for Firebase Analytics is supported out-of-the-box on Android.

Since v3.0.0, analytics tracking is now available in all targets, for use with other analytics trackers.

## Usage

Ballast's Firebase Analytics integration provides automatic tracking of your Inputs to the Firebase Analytics dashboard. 
Firebase Analytics should be integrated in your app [as normal][4], and then you need to add the 
[`ballast-firebase-analytics`](#Installation) dependency and add the Interceptor to your ViewModel configuration. Note 
that the below example uses `AndroidViewModel`, but the `FirebaseAnalyticsInterceptor` will work just the same with any 
other Ballast ViewModel type (Repositories, BasicViewModel, etc.).

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
            this += AnalyticsInterceptor(
                tracker = FirebaseAnalyticsTracker(Firebase.analytics),
                shouldTrackInput = { it.isAnnotatedWith<FirebaseAnalyticsTrackInput>() },
            )
            // helper function for setting up tracking with Firebase
            this += FirebaseAnalyticsInterceptor() // FirebaseAnalyticsInterceptor factory function, which returns AnalyticsInterceptor
        }
        .withViewModel(
            initialState = ExampleContract.State(),
            inputHandler = ExampleInputHandler(),
            name = "Example",
        )
        .build(),
)
```

While Crashlytics takes an opt-out approach to logging Inputs, Analytics is entirely opt-in. Most Inputs in your app 
probably aren't necessary to track, what you're mostly interested in is conversions. The `FirebaseAnalyticsInterceptor` 
will only track Inputs that are annotated with `FirebaseAnalyticsTrackInput`, and ignore the rest. Each Input will be
logged using its `.toString()` value, so be sure to override `.toString()` for any inputs you want tracked to remove any
sensitive info from them.

{% alert 'warning' :: compileAs('md') %}
**Warning**

Make sure any inputs annotated with `@FirebaseAnalyticsTrackInput` do not leak any sensitive information through 
`.toString()`.
{% endalert %}

## Installation

```kotlin
repositories {
    mavenCentral()
}

// for plain JVM or Android projects
dependencies {
    implementation("io.github.copper-leaf:ballast-analytics:{{site.version}}")
    implementation("io.github.copper-leaf:ballast-firebase-analytics:{{site.version}}")
}

// for multiplatform projects
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-analytics:{{site.version}}")
                implementation("io.github.copper-leaf:ballast-firebase-analytics:{{site.version}}")
            }
        }
    }
}
```

[1]: https://firebase.google.com/docs/crashlytics/get-started?platform=android
[2]: https://firebase.google.com/docs/crashlytics/customize-crash-reports?platform=android#add-logs
[3]: https://firebase.google.com/docs/crashlytics/customize-crash-reports?platform=android#log-excepts
[4]: https://firebase.google.com/docs/analytics/get-started?platform=android
