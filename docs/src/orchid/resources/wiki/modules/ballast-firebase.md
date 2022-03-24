---
---

# {{ page.title }}

Ballast's Firebase modules offer an easy, declarative way to send data automatically to Firebase on Android. Analytics 
and Crashlytics are both supported, each with different modules, so you can pick and choose the features you need.

## Crashlytics

Ballast's Crashlytics integration provides automatic tracing of your Inputs and gives you Logs and Keys attached to your
crash reports to aid in identifying and getting to the root cause of your application issues. Crashlytics should be 
integrated in your app [as normal][1], and then 
you need to add the [`ballast-crashlytics`](#Installation) dependency, and add the Interceptor to your ViewModel 
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
            this += FirebaseCrashlyticsInterceptor(Firebase.crashlytics)
        }
        .forViewModel(
            initialState = ExampleContract.State(),
            inputHandler = ExampleInputHandler(),
            name = "Example",
        )
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

In addition to logs, the `FirebaseCrashlyticsInterceptor` will also record any exceptions that are thrown but do not 
crash the app as a [non-fatal exception][3]

## Analytics

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
            this += FirebaseAnalyticsInterceptor(Firebase.analytics)
        }
        .forViewModel(
            initialState = ExampleContract.State(),
            inputHandler = ExampleInputHandler(),
            name = "Example",
        )
)
```

While Crashlytics takes an opt-out approach to logging Inputs, Analytics is entirely opt-in. Most Inputs in your app 
probably aren't necessary to track, what you're mostly interested in is conversions. The `FirebaseAnalyticsInterceptor` 
will only track Inputs that are annotated with `FirebaseAnalyticsTrackInput`, and ignore the rest. Each Input will be
logged using its `.toString()` value, so be sure to override `.toString()` for any inputs you want tracked to remove any
sensitive info from them.

## Installation

```kotlin
repositories {
    mavenCentral()
}

// for plain JVM or Android projects
dependencies {
    implementation("io.github.copper-leaf:ballast-crashlytics:{{site.version}}")
    implementation("io.github.copper-leaf:ballast-firebase-analytics:{{site.version}}")
}

// for multiplatform projects
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-crashlytics:{{site.version}}")
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
