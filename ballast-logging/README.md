# Ballast Logging

## Overview

This module provides platform-specific implementations of Ballast Loggers, as well as n Interceptor to automatically
log the activity of the ViewModel.

## Supported Platforms

| Platform | Supported |
|----------|-----------|
| JVM      | ✅         |
| Android  | ✅         |
| iOS      | ✅         |
| JS       | ✅         |
| WASM JS  | ✅         |

## See Also

- [Ballast Core](./../ballast-core/README.md)

## Usage

Loggers are attached to a ViewModel with in the `BallastViewModelConfiguration`. This logger may be used by any
Interceptor, as well as your own InputHandlers, EventHandlers, or SideJobs. The same instance of the Logger is shared
by all components in the ViewModel.

The `BallastViewModelConfiguration.Builder` is often defined with a common component shared by all ViewModels in your 
application, where all cross-cutting functionality is attached. It is then converted to a 
`BallastViewModelConfiguration.TypedBuilder` using the `builder.withViewModel()` function. It's recommended to define 
your Logger in the common configuration. As such, the `BallastViewModelConfiguration.Builder.logger` property is a 
factory function, and will be passed the name of the ViewModel set in `builder.withViewModel()` to be used as the tag. 
Function references on the Logger class are a clean way to wire this up.

```kotlin
class ExampleViewModel(coroutineScope: CoroutineScope) : BasicViewModel<
        ExampleContract.Inputs,
        ExampleContract.Events,
        ExampleContract.State
        >(
    coroutineScope = coroutineScope,
    config = BallastViewModelConfiguration.Builder()
        .apply {
            logger = ::PrintlnLogger
        }
        .withViewModel(
            initialState = ExampleContract.State(),
            inputHandler = ExampleInputHandler(),
            name = "Example"
        )
        .build(),
    eventHandler = eventHandler { },
)
```

### Platform Loggers

| Logger              | Platform        | Notes                                                                    |
|---------------------|-----------------|--------------------------------------------------------------------------|
| NoOpLogger          | Any             | Disables all logging for the ViewModel                                   |
| PrintlnLogger       | Any             | Formats messages and prints them to stdout via `println`                 |
| AndroidLogger       | Android         | Prints messages directly to Android LogCat without additional formatting |
| NSLogLogger         | iOS             | Formats messages and prints them to NSLog (legacy logger)                |
| OSLogLogger         | iOS             | Formats messages and prints them to OSLog (modern logger)                |
| JsConsoleLogger     | JS Browser      | Formats messages and prints them to `console.log`                        |
| WasmJsConsoleLogger | WASM JS Browser | Formats messages and prints them to `console.log`                        |

### LoggingInterceptor

The `LoggingInterceptor` can be added to automatically log the internal behavior of your ViewModels. This should
typically only be added in debug builds, as it may leak sensitive information in production builds. The 
LoggingInterceptor writes its logs to the logger added in `BallastViewModelConfiguration`. The information logged by 
this interceptor may be quite verbose, but it can be really handy for inspecting the data in your ViewModel and 
determining what happened in what order.

```kotlin
class ExampleViewModel(coroutineScope: CoroutineScope) : BasicViewModel<
        ExampleContract.Inputs,
        ExampleContract.Events,
        ExampleContract.State
        >(
    coroutineScope = coroutineScope,
    config = BallastViewModelConfiguration.Builder()
        .apply {
            if (DEBUG) { // some build-time constant
                logger = ::PrintlnLogger
                interceptors += LoggingInterceptor()
            }
        }
        .withViewModel(
            initialState = ExampleContract.State(),
            inputHandler = ExampleInputHandler(),
            name = "Example"
        )
        .build(),
    eventHandler = eventHandler { },
)
```

## Installation

```kotlin
repositories {
    mavenCentral()
}

// for plain JVM or Android projects
dependencies {
    implementation("io.github.copper-leaf:ballast-logging:{{ballastVersion}}")
}

// for multiplatform projects
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-logging:{{ballastVersion}}")
            }
        }
    }
}
```
