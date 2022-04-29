---
---

# {{ page.title }}

The Ballast Core module provides all the core capabilities of the entire Ballast MVI framework. The Core framework is 
robust and opinionated, but also provides many ways to extend the functionality through Interceptors without impacting
the core MVI model. Any additional functionality outside of Core will typically be implemented as an Interceptor and 
provided to the `BallastViewModelConfiguration`.

# Usage

## ViewModels

The Core module provides several ViewModel base classes, so Ballast can integrate natively with a variety of platforms.

- `AndroidViewModel`: A subclass of `androidx.lifecycle.ViewModel`
- `IosViewModel`: A custom ViewModel that can be integrated with Combine Publishers for SwiftUI
- `BasicViewModel`: A generic ViewModel for Kotlin targets that don't have their own platform-specific ViewModel, or for
  anywhere you want to manually control the lifecycle of the ViewModel. `BasicViewModel`'s lifecycle is controlled by a 
  `coroutineScope` provided to it upon creation. When the scope gets cancelled, the ViewModel gets closed and can not be
  used again.

## Interceptors

The Core module comes with only one Interceptor, 

- `LoggingInterceptor`: It will print all Ballast activity to the logger provided in the `BallastViewModelConfiguration`.
  The information logged by this interceptor may be quite verbose, but it can be really handy for quickly inspecting 
  the data in your ViewModel and what happened in what order.

The `LoggingInterceptor` writes to a logger installed into the `BallastViewModelConfiguration`, which may be used by 
InputHandlers or other Ballast features as well.

{% snippet 'loggers' %}

{% alert 'danger' :: compileAs('md') %}
Be sure to only include `LoggingInterceptor()` and the logger in debug builds, as logging in production may cause 
performance degradation and risks leaking sensitive info through to the application logs. It should not be used to 
create a paper-trail of activity in your app, you should use something like [Ballast Firebase][1] to more selectively
create the paper-trail.

[1]: {{ 'Ballast Firebase' | link }}
{% endalert %}

```kotlin
class ExampleViewModel(coroutineScope: CoroutineScope) : BasicViewModel<
        ExampleContract.Inputs,
        ExampleContract.Events,
        ExampleContract.State>(
  coroutineScope = coroutineScope,
  config = BallastViewModelConfiguration.Builder()
    .apply {
      if(DEBUG) { // some build-time constant
        logger = PrintlnLogger()
        this += LoggingInterceptor()
      }
    }
    .forViewModel(
      initialState = ExampleContract.State(),
      inputHandler = ExampleInputHandler(),
      name = "Example",
    ),
  eventHandler = ExampleEventHandler(),
)
```

## Input Strategies

{% snippet 'inputStrategies' %}

# Installation

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
