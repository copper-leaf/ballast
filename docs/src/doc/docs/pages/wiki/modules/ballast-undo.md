---
---

## Overview

Undo/Redo functionality can be very handy in situations where the user is interacting with larger amounts of data over
a longer period of time. This module implements standard state-based undo/redo behavior making it simple to navigate
through the history of a user's changes over time.

Note that the default functionality is strictly state-based, and it works by observing States emitted from the ViewModel
and restoring captured State when requested, irrespective of any particular Inputs that changed the State. It does not
attempt to undo specific Inputs, which may have performed other actions like emitting Events, starting Side Jobs, or 
other "side effects" which cannot be so easily tracked and undone.

## Usage

Start by creating a `UndoController` for your ViewModel. This controller includes functions to `undo()` and `redo()` 
which should be called from the UI, as well as corresponding `Flows` which notify whether such actions are can be used.
A default implementation, `DefaultUndoController` may be used, but for advanced use-cases such as persisting the 
undo/redo state across application restarts, you may implement your own.

Then, set up your ViewModel with the `BallastUndoInterceptor` added, which needs that Controller we just created. 

```kotlin
class ExampleViewModel(
    coroutineScope: CoroutineScope,
    controller: UndoController<
        ExampleContract.Inputs,
        ExampleContract.Events,
        ExampleContract.State>,
) : BasicViewModel<
        ExampleContract.Inputs,
        ExampleContract.Events,
        ExampleContract.State>(
    coroutineScope = coroutineScope,
    config = BallastViewModelConfiguration.Builder()
        .apply {
            this += BallastUndoInterceptor(controller)
        }
        .withViewModel(
            initialState = ExampleContract.State(),
            inputHandler = ExampleInputHandler(),
            name = "Example",
        )
        .build(),
)

@Composable
fun mainUi() {
    val controller = remember { DefaultUndoController<ExampleContract.Inputs, ExampleContract.Events, ExampleContract.State>() }
    val applicationCoroutineScope = rememberCoroutineScope()
    val viewModel = remember(applicationCoroutineScope, controller) { ExampleViewModel(applicationCoroutineScope, controller) }
    val uiState by viewModel.observeStates().collectAsState()
    
    // buttons to undo/redo 
    val isUndoAvailable by undoController.isUndoAvailable.collectAsState(false)
    val isRedoAvailable by undoController.isRedoAvailable.collectAsState(false)
    Button(onClick = { controller.undo() }, enabled = isUndoAvailable) { Text("Undo") }
    Button(onClick = { controller.redo() }, enabled = isRedoAvailable) { Text("Redo") }

    // the normal content for this screen, which will be updated via undo/redo as prompted by the user 
    Content(uiState) {
        viewModel.trySend(it)
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
    implementation("io.github.copper-leaf:ballast-undo:{{gradle.version}}")
}

// for multiplatform projects
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-undo:{{gradle.version}}")
            }
        }
    }
}
```
