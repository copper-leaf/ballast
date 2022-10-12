---
---

# {{ page.title }}

## Overview

Ballast Sync allows you to share the state of your ViewModel across multiple instances, potentially even over a network.
It allows you to build your ViewModels as normal, and then choose one to be the "source of truth" for the other
ViewModels will share the synchronized state, and optionally allow those "observing" ViewModels to send changes back to
the source. The flow of data within a synchronized ViewModels is all asynchronous, and follows a model of
"eventual consistency".

## Usage

There are 3 types of ViewModels which may share in the synchronized state:

- `Source`: The Source ultimately drives the state of the other ViewModels. Anytime its own State gets changed, that
  updated State will be sent back to all other ViewModels that are observing it. There should only be 1 Source ViewModel
  in a given Connection, otherwise they will all be competing to be the source of truth, which may lead to infinite
  recursion. If all synchronization is performed locally, it's up to you to make sure there is only 1 ViewModel 
  registered as Source. If you're connecting over a network, it's best to keep the Source ViewModel on the Server, and 
  only use Replicas or Spectators within the client applications.
- `Replica`: Replicas are ViewModels that share the same Contract and InputHandler as the Source ViewModel, but will
  ultimately reflect the State of the Source. Any Inputs sent to it will be processed locally, and then sent back to the
  Source to be processed there as well, at which point the Source's State will eventually overwrite the Replica's own
  State. You can add as many Replicas as you wish.
- `Spectator`: Spectators work just like Replicas, except that their Inputs are not sent to the Source. Thus, a user can
  interact with a Spectator ViewModel without those changes impacting any others. But like Replicas, changes to the
  Source will eventually overwrite the Spectator's own state. You can add as many Spectators as you wish.

This module also uses an abstract `SyncConnectionAdapter` to actually perform the synchronization. Currently, only an
in-memory sync adapter is provided, and no serialization is necessary for this as both States and Inputs are
synchronized fully in memory. You may implement your own adapter to sync this data over a network or via some other
mechanism if necessary (for example, websockets, Redis, etc.).

Additionally, the Adapter will be wrapped in a `SyncConnection` which handles the logic for determining how States and
Inputs are actually synchronized amongst each other. `DefaultSyncConnection` is what actually handles the Source,
Replica, and Spectator behavior as listed above, but you may create your own implementation if you need to perform some
other kind of behavior.

To get started, first build your ViewModel as normal. Next, provide the `BallastSyncInterceptor` with your
`SyncConnection` and `SyncConnectionAdapter`. The Interceptor forwards all ViewModel data to the Connection, which
decides how its data should be synchronized, and then forwards the relevant data to the Adapter, which will send it to
its connected ViewModel Clients.

```kotlin
// InMemorySyncAdapter must shared among all connected ViewModels
private val syncAdapter = InMemorySyncAdapter<
    CounterContract.Inputs,
    CounterContract.Events,
    CounterContract.State>()

class ExampleViewModel(
    coroutineScope: CoroutineScope,
    syncClientType: DefaultSyncConnection.ClientType,
) : BasicViewModel<
    ExampleContract.Inputs,
    ExampleContract.Events,
    ExampleContract.State>(
    coroutineScope = coroutineScope,
    config = BallastViewModelConfiguration.Builder()
        .apply {
            this += BallastSyncInterceptor( // connects the ViewModel to the Connection
                connection = DefaultSyncConnection( // implements the logic for deciding what to sync
                    clientType = syncClientType,
                    adapter = syncAdapter, // perform the actual synchronization among the connected clients
                ),
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
    implementation("io.github.copper-leaf:ballast-sync:{{site.version}}")
}

// for multiplatform projects
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-sync:{{site.version}}")
            }
        }
    }
}
```
