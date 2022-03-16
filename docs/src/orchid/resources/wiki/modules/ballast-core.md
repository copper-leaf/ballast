---
---

# Ballast Core

The Ballast Core module provides all the core capabilities of the entire Ballast MVI framework. The Core framework is 
robust and opinionated, but also provides many ways to extend the functionality through Interceptors without impacting
the core MVI model. Any additional functionality outside of Core will typically be implemented as an Interceptor and 
provided to the `BallastViewModelConfiguration`.

## ViewModels

The Core module provides several ViewModel base classes, so Ballast can integrate natively with a variety of platforms.

- `AndroidViewModel`: A subclass of `androidx.lifecycle.ViewModel`
- `IosViewModel`: A custom ViewModel that is tied to an iOS `ViewController`'s lifecycle
- `BasicViewModel`: A generic ViewModel which can be used in any arbitrary context, including Kotlin targets that don't
  have their own platform-specific ViewModel. `BasicViewModel`'s lifecycle is controlled by a `coroutineScope` provided
  to it upon creation.

## Interceptors

The Core module comes with only one Interceptor, `LoggingInterceptor`. It will print all Ballast activity to the logger
provided to the `BallastViewModelConfiguration`. Ballast Core also includes two different Logger implementations, 
`NoOpLogger`, which is used by default and simply discards all logs, and `PrintlnLogger` which writes all logs to 
`println()`. Be sure to only include `LoggingInterceptor()` in debug builds, as logging in production may cause 
performance degradation, and risks leaking sensitive info through the logs as there is no log filtering capability 
included.

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
            logger = PrintlnLogger()
            this += LoggingInterceptor()
        }
        .forViewModel(
            initialState = ExampleContract.State(),
            inputHandler = ExampleInputHandler(),
            name = "Example",
        )
)
```

## Input Strategies

Ballast offers 3 different Input Strategies out-of-the-box, which each adapt Ballast's core functionality for different
applications:

- `LifoInputStrategy`: A last-in-first-out strategy for handling Inputs, and the default strategy if none is provided.
  Only 1 Input will be processed at a time, and if a new Input is received while one is still working, the running Input
  will be cancelled to immediately accept the new one. Corresponds to `Flow.collectLatest { }`, best for UI ViewModels.
- `FifoInputStrategy`: A first-in-first-out strategy for handling Inputs. Inputs will be processed in the same order
  they were sent and only ever one-at-a-time, but instead of cancelling running Inputs, new ones are queued and will be
  consumed later when the queue is free. Corresponds to the normal `Flow.collect { }`, best for non-UI ViewModels.
- `ParallelInputStrategy`: For specific edge-cases where neither of the above strategies works. Inputs are all handled
  concurrently so you don't have to worry about blocking the queue or having Inputs cancelled. However, it places
  additional restrictions on State reads/changes to prevent usage that might lead to race conditions.

Ballast Core also includes `DefaultGuardian`, which you can use if you need to create your own `InputStrategy` to 
maintain the same level of safety as the core `InputStrategies`.

## Installation

```kotlin
repositories {
    mavenCentral()
}

// for plain JVM or Android projects
dependencies {
    implementation("io.github.copper-leaf:ballast-core:{{site.version}}")
}

// for multiplatform projects
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-core:{{site.version}}")
            }
        }
    }
}
```
