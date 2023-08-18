---
---

# {{ page.title }}

## Overview

MVI has been known for a while as a great option for managing UI state, but most applications will also need to manage
some state that lives longer than a single screen. This would be things like account management, or caching of expensive
computations or API calls, and MVI can actually be a great fit for this Repository Layer, too. The [Repository Layer][1]
has a lifetime that is longer than any single screen, and acts as a liaison between your UI code (the typical MVI area) 
and the domain objects that make the UI work.

On Android, it's recommended to have a [Data Layer][2], but exactly how to build it is not well known, and there really 
aren't any recommendations from Google, either. [Dropbox Store][3] attempted to step in and create a library to 
implement this Data or Repository layer, but in practice it works more like a persistent cache than a true solution for
app-wide State management.

Ballast Repository aims to fill that gap, and provide an opinionated way to manage the data in your application layer, 
using the same MVI model you're used to with your UI code. One huge benefit of using Ballast as your repository layer
vs other solutions, is that you can approach both UI and non-UI development with the same mindset; you don't have to 
"context switch" when moving between layers!

Ballast Repository is built around 3 core concepts: the MVI model as implemented with a special `BallastRepository` 
ViewModel, the `Cached<T>` interface to hold and update data within the Repository, and the `EventBus` to facilitate
communication between Repository instances throughout the entire layer.

## Example Use-Case

Before diving into the usage of the Repository module, it may be helpful to get a basic intuition for when you might 
need it, and how this layer of your application is intended to work. Consider the following situation:

You have an app where users can log on and view how much they've used your service, and how much it costs them. The 
users may have multiple linked accounts and switch between the accounts freely. Viewing their usage is tied to the
individual account, but billing is aggregated among all accounts to simplify paying the bill. 

We want to minimize the number of API calls for a snappy user-experience, so we cache every API response. Whenever the 
user changes the current account, we want to refresh their usage data, but not the billing info, since we want to show
the new usage data for the new account, but the billing data does not need to be changed.

In this model, using a [BallastRepository](#BallastRepository), we would hold the user account info in an 
`AccountRepository`, the usage data in `UsageRepository`, and billing info in `BillingRepository`. All the cached data 
is held within a [`Cached<T>`](#Cached) property of each Repository's State. Changing accounts involves sending an Input 
to `AccountRepository`, which then makes its own changes and then sends the relevant Input through the 
[`EventBus`](#EventBus) to the `BillingRepository`. The UI layer does not need to know any specifics of what's going on 
in the Repository layer, as it just passively observes the `Cached` properties. Furthermore, it also does not need to 
know anything about the specific organization of data in it, when changing one property needs to clear the cache of 
another, etc. You can easily wire up any screen to change the account or fetch the usage/billing info, trust that it 
will be fetched only once if needed or else returned from the cache, and know that the relevant UI will be updated 
automatically whenever the repository finished updating its cached without having to do any specific UI handling for 
that. 

## Usage

### BallastRepository

`BallastRepository` is a special `BallastViewModel` implementation that is intended to be used as the "ViewModel" of 
your Repository layer. Unlike UI ViewModels, the Repositories do not have `EventHandlers`, as Events sent from the 
Repository InputHandler are sent to the EventBus instead (which is simply a SharedFlow). It also uses the 
`FifoInputStrategy` to ensure that all Inputs are handled, rather than being dropped or cancelled, though they're still
processed one-at-a-time.

Repositories need a `CoroutineScope` to control their lifetime (commonly a single, glogal Application CoroutineScope), 
and the `EventBus` instance, which should be shared among all Repositories. There also exists a 
`AndroidBallastRepository` which implements the same semantics, but is an instance of `androidx.lifecycle.ViewModel` and
so can be scoped to a Navigation sub-graph.

```kotlin
class ExampleRepositoryImpl(
    coroutineScope: CoroutineScope,
    eventBus: EventBus,
) : BallastRepository<
        ExampleRepositoryContract.Inputs,
        ExampleRepositoryContract.State>(
    coroutineScope = coroutineScope,
    eventBus = eventBus,
    config = BallastViewModelConfiguration.Builder()
        .apply {
            initialState = ExampleRepositoryContract.State()
            inputHandler = ExampleRepositoryInputHandler()
            name = "Example Repository"
        }.build()
)
```

The `Contract` for a Repository can be anything you need it to be, but a common implementation based around Ballast's 
own `Cached<T>` interface looks like the example below. You can add as many cached properties to the same Repository as
needed, but they should typically be related by domain. 

```kotlin
object ExampleRepositoryContract {
    data class State(
        val initialized: Boolean = false,
        
        val examplePropertyInitialized: Boolean = false,
        val exampleProperty: Cached<ExampleValue> = Cached.NotLoaded(),
    )

    sealed class Inputs {
        data object ClearCaches : Inputs()
        data object Initialize : Inputs()
        data object RefreshAllCaches : Inputs()

        data class RefreshExampleProperty(val forceRefresh: Boolean) : Inputs()
        data class ExamplePropertyUpdated(val value: Cached<ExampleValue>) : Inputs()
    }
}
```

The corresponding InputHandler is also very much templated, using the `fetchWithCache()` function to determine when to
update the cached value:

```kotlin
class ExampleRepositoryInputHandler(
    private val exampleApi: ExampleApi,
) : InputHandler<
    ExampleRepositoryContract.Inputs,
    Any,
    ExampleRepositoryContract.State> {

    override suspend fun InputHandlerScope<
        ExampleRepositoryContract.Inputs,
        Any,
        ExampleRepositoryContract.State>.handleInput(
        input: ExampleRepositoryContract.Inputs
    ) = when (input) {
        is ExampleRepositoryContract.Inputs.ClearCaches -> {
            updateState { ExampleRepositoryContract.State() }
        }
        is ExampleRepositoryContract.Inputs.Initialize -> {
            val previousState = getCurrentState()

            if (!previousState.initialized) {
                updateState { it.copy(initialized = true) }
                // start observing flows here
                logger.debug("initializing")
                observeFlows(
                    key = "Observe account changes",
                    params.eventBus
                        .observeInputsFromBus<ExampleRepositoryContract.Inputs>(),
                )
            } else {
                logger.debug("already initialized")
                noOp()
            }
        }

        is ExampleRepositoryContract.Inputs.RefreshAllCaches -> {
            // refresh all the caches in this repository
            val currentState = getCurrentState()
            if (currentState.examplePropertyInitialized) {
                postInput(ExampleRepositoryContract.Inputs.RefreshExampleProperty(true))
            }

            Unit
        }
        
        is ExampleRepositoryContract.Inputs.RefreshExampleProperty -> {
            updateState { it.copy(examplePropertyInitialized = true) }
            fetchWithCache(
                input = input,
                forceRefresh = input.forceRefresh,
                getValue = { it.exampleProperty },
                updateState = { ExampleRepositoryContract.Inputs.ExamplePropertyUpdated(it) },
                doFetch = {
                    exampleApi.fetchValue()
                },
            )
        }
        is ExampleRepositoryContract.Inputs.ExamplePropertyUpdated -> {
            updateState { it.copy(value = input.value) }
        }
    }
}
```

The final piece of the puzzle is where things start to look a bit different from normal UI MVI usage. A Ballast 
Repository typically shouldn't be directly exposed to the UI, but instead hidden behind an interface so the UI layers 
don't need to worry about sending the right Inputs and the right time to clear the caches, etc. Instead the UI just 
requests data from the Repository interface as normal and receives the data it needs as a flow, while the Ballast 
Repository does all the work in the background to fetch or return cached data.

```kotlin
public interface ExampleRepository { 
    fun getExampleValue(refreshCache: Boolean): Flow<Cached<ExampleValue>>
}
```

The class that extends `BallastRepository` should then also implement the interface, and send the correct Inputs as the
UI requests data. This makes the actual fetches of data lazy.

```kotlin
class ExampleRepositoryImpl(
    coroutineScope: CoroutineScope,
    eventBus: EventBus,
) : BallastRepository<
        ExampleRepositoryContract.Inputs,
        ExampleRepositoryContract.State>(
    coroutineScope = coroutineScope,
    eventBus = eventBus,
    config = BallastViewModelConfiguration.Builder()
        .apply {
            initialState = ExampleRepositoryContract.State()
            inputHandler = ExampleRepositoryInputHandler()
            name = "Example Repository"
        }.build()
), ExampleRepository {

    override fun getExampleValue(refreshCache: Boolean): Flow<Cached<ExampleValue>> { 
        trySend(ExampleRepositoryContract.Inputs.Initialize)
        trySend(ExampleRepositoryContract.Inputs.RefreshExampleProperty(refreshCache))
        return observeStates()
            .map { it.exampleProperty }
    }
    
}
```

There is a lot of boilerplate to this method, and eventually there may be a generic Caching Repository to do all this 
for you. But for now, it's best to just be explicit, so you can easily track what data is being changed and at what time
within each Repository.

### EventBus

The `EventBus` class is basically just a wrapper around a `SharedFlow`. It should share the same instance among all 
Repositories, so that one Repository can post an event to the bus, and it will be delivered to another Repository.

Each Repository should typically observe values of its own type from the EventBus, using 
`eventBus.observeInputsFromBus<ExampleRepositoryContract.Inputs>()`, but you're free to observe values of any type. An 
example is using a generic "ClearCache" token sent to the bus, and all repositories can watch for that token and clear
themselves.

Values can be sent from one Repository to another with the normal `InputHandlerScope.postEvent()`. You can post any 
non-null value, as the `Events` type is `Any`.

### Cached

`Cached` is a sealed class which holds the data in your Repository and notifies observers of all changes to that value 
as it is loaded. It can be one of 4 states: `NotLoaded`, `Fetching`, `Value`, or `FetchingFailed`. 

For values that need to be loaded once from some remote source or expensive computation, use `fetchWithCache()` within
your InputHandler in response to a `Refresh*` Inputs. That function takes care of determining when to fetch new values
and capturing errors from the fetcher. But one particular feature of it is that when a hard refresh is requested, the
state will change the previously-cached value will be carried through those states until a new value finally returns, 
which can be used to show a progress indicator in the UI with the old values, rather than clearing the entire screen
while loading. The `Cached<T>` value has a number of extension functions to help in displaying the right things in the
UI according to the status of that cached value.

When a UI ViewModel is observing a `Cached<T>` property from a Repository, you should think of it as if the UI ViewModel 
simply observes a "view" of the repository. Technically, the cached values will be copied into the UI ViewModel, but 
there shouldn't be any reason to change the value directly in the UI ViewModel. Instead, send those changes back to the
Repository and wait for it to get changed there, at which point the updated value will flow back into the UI ViewModel. 
Also, do not unwrap the Cached value in the UI ViewModel, continue to hold onto it as the wrapped `Cached<T>` value so 
that the UI can use the Cached DSL to optimize its display of the inner value.

## Installation

```kotlin
repositories {
    mavenCentral()
}

// for plain JVM or Android projects
dependencies {
    implementation("io.github.copper-leaf:ballast-repository:{{site.version}}")
}

// for multiplatform projects
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-repository:{{site.version}}")
            }
        }
    }
}
```

[1]: https://docs.microsoft.com/en-us/previous-versions/msp-n-p/ff649690(v=pandp.10)?redirectedfrom=MSDN
[2]: https://developer.android.com/jetpack/guide#data-layer
[3]: https://github.com/dropbox/Store
