# Ballast Kotlinx Serialization

## Overview

Adds automatic JSON serialization/deserialization capabilities to ViewModels with 
[Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization). This allows you to register `KSerializers` 
once for the entire ViewModel, then all Ballast Plugins in that ViewModel can serialize their Inputs, Events, and States
using those serializers.

## Supported Platforms

| Platform | Supported |
|----------|-----------|
| JVM      | ✅         |
| Android  | ✅         |
| iOS      | ✅         |
| JS       | ✅         |
| WASM JS  | ✅         |

## See Also

- [Ballast Saved State](./../ballast-saved-state)
- [Ballast Debugger Client](./../ballast-debugger-client)
- [Ballast Queue ViewModel](./../ballast-queue-viewmodel)

## Usage

Ballast ViewModels contain `encoder` and `decoder` properties in their `BallastViewModelConfiguration`, which are used
anytime a ViewModel or Plugin needs to convert an `Input`, `Event`, or `State` object to a String, whether for logging
or for transport over a network, or for persistent storage. The default ViewModel configuration uses `.toString()` to 
convert an object to a String, but does not include support for deserializing an object from a String.

This module adds a simple `withSerialization()` function to the `BallastViewModelConfiguration.TypedBuilder` allowing 
you to register `KSerializers` which get used for all of a ViewModel's serialization and deserialization tasks.

```kt
class ExampleViewModel(
    private val coroutineScope: CoroutineScope,
) : BasicViewModel<
        ExampleContract.Inputs,
        ExampleContract.Events,
        ExampleContract.State>(
    coroutineScope = coroutineScope,
    config = BallastViewModelConfiguration.Builder()
        .withViewModel(
            inputHandler = ExampleInputHandler(),
            initialState = ExampleContract.State,
            name = "ExampleViewModel",
        )
        .withJsonSerialization(
            inputsSerializer = ExampleContract.Inputs.serializer(),
            eventsSerializer = ExampleContract.Events.serializer(),
            stateSerializer = ExampleContract.State.serializer(),
            json = Json { prettyPrint = true }, // optional
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
    implementation("io.github.copper-leaf:ballast-kotlinx-serialization:{{ballastVersion}}")
}

// for multiplatform projects
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-kotlinx-serialization:{{ballastVersion}}")
            }
        }
    }
}
```
