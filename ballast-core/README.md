# Ballast Core

## Overview

The Ballast Core module provides all the core capabilities of the entire Ballast MVI framework. This module is simply an
aggregation of other fundamental Ballast modules, which are combined to provide the basic functionality and 
platform-specific integrations needed for developing application, and is the primary module you should include when
using Ballast for building applications. Library developers building additional features or integrations into Ballast 
should depend on [Ballast API](./../ballast-api) instead, since a library should not need the 
platform-specific features provided by the other modules.

## Supported Platforms

| Platform | Supported |
|----------|-----------|
| JVM      | ✅         |
| Android  | ✅         |
| iOS      | ✅         |
| JS       | ✅         |
| WASM JS  | ✅         |

## See Also

- [Ballast API](./../ballast-api)
- [Ballast Viewmodel](./../ballast-viewmodel)
- [Ballast Logging](./../ballast-logging)
- [Ballast Utils](./../ballast-utils)

## Usage

`ballast-core` is the standard starting point for most Ballast applications. Adding it to your dependencies brings in
`ballast-api`, `ballast-viewmodel`, `ballast-logging`, and `ballast-utils` together, providing everything needed to
create and run ViewModels.

At a high level, Ballast is a library to help you manage the state of your application as it changes over time. It
follows the basic pattern of MVI, where the ViewModel state cannot be changed directly — instead you send your _intent_
to change the state to the library. The library processes those requests safely, in a way that is predictable and
repeatable, which generates new states that flow back to the UI automatically:

```
UI --[Inputs]--> ViewModel --[State]--> UI
```

The general workflow for building a Ballast screen involves:

1. Define a Contract
2. Write the InputHandler
3. Write the EventHandler
4. Combine everything into a ViewModel
5. Connect the ViewModel to your UI

### Contract

The Contract is the declarative model of what is happening in a screen. It provides a structure for what data will be
changing (the State) and how you will be interacting with it (Inputs), giving you a single place to understand
everything about any given screen.

The contract is canonically a single top-level `object` with a name like `*Contract`, and it contains 3 nested
classes: `State`, `Inputs`, and `Events`. If you're using Ballast in a multiplatform project, the Contract should be in
the `commonMain` sourceSet.

```kotlin
object LoginScreenContract {
    data class State(
        val username: TextFieldValue = TextFieldValue(),
        val password: TextFieldValue = TextFieldValue(),
        val loggingIn: Boolean = false,
    )

    sealed interface Inputs {
        data class UsernameChanged(val newValue: TextFieldValue) : Inputs
        data class PasswordChanged(val newValue: TextFieldValue) : Inputs
        data object LoginButtonClicked : Inputs
        data object RegisterButtonClicked : Inputs
    }

    sealed interface Events {
        data object NavigateToDashboard : Events
        data object NavigateToRegistration : Events
    }
}
```

#### State

The most important component of the MVI contract is the State. All data in your UI that changes meaningfully should be
modeled in your State. States are held in-memory and are guaranteed to always exist through the `StateFlow`. How you
build your UI and model your Inputs should be derived completely from how you model your State.

State is modeled as a Kotlin immutable `data class`. While some MVI frameworks suggest using a `sealed class` for UI
state, Ballast's opinion is that the State should be a `data class` — real-world UIs are rarely cleanly delineated
between such discrete states, and commonly have many features that must all be modeled simultaneously. `sealed classes`
work great as individual properties _within_ that State, though.

#### Inputs

Inputs are the core of how Ballast does all its processing. The "intent" a user has when interacting with the UI is
captured into an Input class and sent to the ViewModel to be processed. Inputs are modeled as a Kotlin `sealed interface`.

A good rule of thumb: avoid re-using any Input for more than one purpose. It should be entirely clear what an Input
will do to the State without having to look at its implementation. If you are tempted to re-send the same Input to do
2 different things, it should just be 2 different Inputs.

#### Events

Events are one-off side effects that must be handled exactly once at the appropriate time — such as navigation requests.
Events are sent from the InputHandler and delivered to the EventHandler, keeping platform-specific event-handling logic
out of the ViewModel. Like Inputs, Events are modeled as a Kotlin `sealed interface`.

> **Note:** Ballast processes Events with a `Channel`, providing an "at-most once" delivery model. If your application
> requires stronger delivery guarantees, consider modeling those cases as State instead.

### InputHandler

The InputHandler is the only place in the MVI loop that is allowed to run arbitrary code. It implements the
`InputHandler` interface and receives Inputs from the queue one at a time. The `InputHandlerScope` DSL can update
ViewModel State, post Events, start side jobs, and call any other suspending functions.

If you're using Ballast in a multiplatform project, the InputHandler should be in the `commonMain` sourceSet.

```kotlin
import LoginScreenContract.*

class LoginScreenInputHandler(
    private val loginRepository: LoginRepository,
) : InputHandler<Inputs, Events, State> {
    override suspend fun InputHandlerScope<Inputs, Events, State>.handleInput(
        input: Inputs
    ) = when (input) {
        is UsernameChanged -> updateState { copy(username = input.newValue) }
        is PasswordChanged -> updateState { copy(password = input.newValue) }
        is LoginButtonClicked -> {
            updateState { copy(loggingIn = true) }
            sideJob("login") {
                val success = loginRepository.login(
                    getState().username.text,
                    getState().password.text,
                )
                if (success) postEvent(Events.NavigateToDashboard)
                else postInput(Inputs.LoginFailed)
            }
        }
        is RegisterButtonClicked -> postEvent(Events.NavigateToRegistration)
    }
}
```

#### Side Jobs

Side jobs allow you to start coroutines that run in the "background" of your ViewModel, alongside the normal Input
queue. They are bound by the same lifecycle as the ViewModel and can collect from infinite flows.

```kotlin
sideJob("key") {
    infiniteFlow()
        .map { Inputs.SomeInputType() }
        .onEach { postInput(it) }
        .launchIn(this)
}
```

Side jobs cannot directly access or modify the ViewModel State, but can post Inputs and Events back to the ViewModel to
request state changes.

### EventHandler

The EventHandler handles Events sent from the ViewModel to the UI, and is the exact counterpart of the InputHandler.
Inputs flow from the UI into the ViewModel; Events flow from the ViewModel out to the UI. The EventHandler may be
attached and detached dynamically in response to the UI's lifecycle — Events sent while detached will be queued and
delivered once the UI is back in a valid state.

```kotlin
import LoginScreenContract.*

class LoginScreenEventHandler(
    private val navigator: Navigator,
) : EventHandler<Inputs, Events, State> {
    override suspend fun EventHandlerScope<Inputs, Events, State>.handleEvent(
        event: Events
    ) = when (event) {
        is Events.NavigateToDashboard -> navigator.navigateToDashboard()
        is Events.NavigateToRegistration -> navigator.navigateToRegistration()
    }
}
```

### ViewModel

The ViewModel combines everything together using `BallastViewModelConfiguration.Builder`. The exact base class varies
by platform — see [Ballast Viewmodel](./../ballast-viewmodel) for platform-specific details — but all configurations
look similar:

```kotlin
// androidMain
class LoginScreenViewModel(
    private val loginRepository: LoginRepository,
) : AndroidViewModel<
    LoginScreenContract.Inputs,
    LoginScreenContract.Events,
    LoginScreenContract.State>(
    config = BallastViewModelConfiguration.Builder()
        .apply {
            this += LoggingInterceptor()
            logger = { AndroidBallastLogger(it) }
        }
        .withViewModel(
            initialState = LoginScreenContract.State(),
            inputHandler = LoginScreenInputHandler(loginRepository),
            name = "LoginScreen",
        )
        .build()
)

// other platforms (JS, Desktop, iOS, etc.)
class LoginScreenViewModel(
    coroutineScope: CoroutineScope,
    loginRepository: LoginRepository,
    navigator: Navigator,
) : BasicViewModel<
    LoginScreenContract.Inputs,
    LoginScreenContract.Events,
    LoginScreenContract.State>(
    config = BallastViewModelConfiguration.Builder()
        .apply {
            this += LoggingInterceptor()
            logger = { JsConsoleBallastLogger(it) }
        }
        .withViewModel(
            initialState = LoginScreenContract.State(),
            inputHandler = LoginScreenInputHandler(loginRepository),
            name = "LoginScreen",
        )
        .build(),
    eventHandler = LoginScreenEventHandler(navigator),
    coroutineScope = coroutineScope,
)
```

### Input Strategies

Ballast offers 3 different Input Strategies out-of-the-box, which each adapt Ballast's core functionality for different
applications:

- **`LifoInputStrategy`**: A last-in-first-out strategy, and the default if none is provided. Only 1 Input is processed
  at a time; if a new Input is received while one is still processing, the running Input is cancelled to immediately
  accept the new one. Corresponds to `Flow.collectLatest { }`. Best for UI ViewModels that need a highly responsive UI
  where you do not want to block the user's actions.

- **`FifoInputStrategy`**: A first-in-first-out strategy. Inputs are processed in order, one at a time. Instead of
  cancelling running Inputs, new ones are queued and consumed later when the queue is free. Corresponds to the normal
  `Flow.collect { }`. Best for non-UI ViewModels, or UI ViewModels where it is acceptable to "block" the UI while
  something is loading.

- **`ParallelInputStrategy`**: For specific edge-cases where neither of the above strategies works. Inputs are all
  handled concurrently, but this places additional restrictions on State reads/changes to prevent race conditions.

> **Warning:** For historical reasons, `LifoInputStrategy` is the default, but it can be unintuitive and cause subtle
> issues in your application. It is recommended to explicitly choose `FifoInputStrategy` unless you are familiar enough
> with Ballast to understand the full implications of `LifoInputStrategy`. This default will likely change to
> `FifoInputStrategy` in a future version, so it is best to always set the strategy explicitly rather than relying on
> the default.

Set the input strategy in the configuration builder:

```kotlin
BallastViewModelConfiguration.Builder()
    .apply {
        inputStrategy = FifoInputStrategy.typed()
    }
    .withViewModel(
        initialState = State(),
        inputHandler = ExampleInputHandler(),
        name = "Example",
    )
    .build()
```

### Interceptors

One of the primary features of Ballast is its interceptor plugin API. Because the MVI pattern decouples the _intent_ to
do work from the actual processing of that work, it is possible to intercept all objects moving through the ViewModel
and add useful functionality without requiring any changes to the Contract or Handler code.

Interceptors receive `BallastNotification`s from the ViewModel at every step of processing (queued, started,
completed, failed, etc.):

```kotlin
class CustomInterceptor<Inputs : Any, Events : Any, State : Any> : BallastInterceptor<Inputs, Events, State> {
    fun BallastInterceptorScope<Inputs, Events, State>.start(
        notifications: Flow<BallastNotification<Inputs, Events, State>>,
    ) {
        launch(start = CoroutineStart.UNDISPATCHED) {
            notifications.awaitViewModelStart()
            notifications
                .onEach { /* observe notifications */ }
                .collect()
        }
    }
}
```

Add interceptors to the configuration builder:

```kotlin
BallastViewModelConfiguration.Builder()
    .apply {
        this += LoggingInterceptor()
        this += BallastDebuggerInterceptor(debuggerConnection)
    }
    .withViewModel(...)
    .build()
```

Ballast provides many built-in interceptors through its various modules. See the [See Also](#see-also) links and the
other modules in this repository for what's available.

## Installation

```kotlin
repositories {
    mavenCentral()
}

// for plain JVM or Android projects
dependencies {
    implementation("io.github.copper-leaf:ballast-core:{{ballastVersion}}")
}

// for multiplatform projects
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-core:{{ballastVersion}}")
            }
        }
    }
}
```
