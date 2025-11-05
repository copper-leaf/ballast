---
---

## Overview

Ballast ViewModels are held entirely in memory, but there are lots of cases where the ViewModel state needs to be saved
in one session and restored in another. The traditional way to do this is to put all that saving/loading logic within 
the InputHandler itself, but this can become messy and error-prone. 

The Saved State module implements the same kind of save/restore state functionality as an Interceptor. Using an 
Interceptor ensures that all changes to the State are persisted, and ensures that the ViewModel does nothing else while
the State is being loaded.

Ballast Saved State offers a standard API to let you save the State to any persistent store you wish, but also offers
out-of-the-box integration with `SavedStateHandle`.

## Usage

Start by creating a `SavedStateAdapter` for your ViewModel. This adapter includes functions to `save()` and `restore()`
the state, which will get called at the appropriate times. 

`restore()` will be called initially when the `ViewModelStarted` is sent, and requires that no other Inputs get sent 
until after the State has been restored. If you need to do some additional initialization after the State has been 
loaded, you can override `onRestoreComplete()` to send an Input back to the VM once the State has been restored.

The `save()` function will be called anytime the State gets updated. You can use the `saveDiff()` function to save 
individual properties of the State only when they've changed, to reduce unnecessary writes.

```kotlin
class ExampleSavedStateAdapter(
    private val database: ExampleDatabase,
) : SavedStateAdapter<
    ExampleContract.Inputs,
    ExampleContract.Events,
    ExampleContract.State> {

    override suspend fun SaveStateScope<
        ExampleContract.Inputs,
        ExampleContract.Events,
        ExampleContract.State>.save() {
        
        saveDiff({ values }) { values ->
            database.saveValues(values)
        }
    }

    override suspend fun RestoreStateScope<
        ExampleContract.Inputs,
        ExampleContract.Events,
        ExampleContract.State>.restore(): ExampleContract.State {
        return ExampleContract.State(
            values = database.selectValues(values)
        )
    }
}
```

Then, set up your ViewModel with the `BallastSavedStateInterceptor` added, which needs that Adapter we just created

```kotlin
class ExampleViewModel(
    coroutineScope: CoroutineScope,
    database: ExampleDatabase,
) : BasicViewModel<
        ExampleContract.Inputs,
        ExampleContract.Events,
        ExampleContract.State>(
    coroutineScope = coroutineScope,
    config = BallastViewModelConfiguration.Builder()
        .apply {
            this += BallastSavedStateInterceptor(
                ExampleSavedStateAdapter(database)
            )
        }
        .withViewModel(
            initialState = ExampleContract.State(),
            inputHandler = ExampleInputHandler(),
            name = "Example",
        )
        .build(),
)
```

## Installation

```kotlin
repositories {
    mavenCentral()
}

// for plain JVM or Android projects
dependencies {
    implementation("io.github.copper-leaf:ballast-saved-state:{{gradle.version}}")
}

// for multiplatform projects
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-saved-state:{{gradle.version}}")
            }
        }
    }
}
```
