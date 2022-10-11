---
---

# High-level Feature Overview

## ViewModels

The ViewModel is Ballast's container for implementing the MVI pattern. It holds onto all data and assembles your
components together to process work in a predictable manner. There are a number of ViewModel implementations provided by
Ballast to run in a variety of scenarios, such as:

- `AndroidViewModel`: A subclass of `androidx.lifecycle.ViewModel`
- `IosViewModel`: A custom ViewModel that is tied to an iOS `ViewController`'s lifecycle
- `BasicViewModel`: A generic ViewModel which can be used in any arbitrary context, including Kotlin targets that don't
  have their own platform-specific ViewModel. `BasicViewModel`'s lifecycle is controlled by a `coroutineScope` provided
  to it upon creation.

Typically, a single ViewModel serves as the store for a single Screen, and is not shared among multiple screens. Data
that should persist through multiple screens should either be passed directly through the navigation request, or be
managed by your [repository layer][3] and re-fetched from the later screen.

## Contracts

The Contract is a declarative model what is happening in a screen. The Contract is entirely separate from any Ballast
APIs, so while the snippet below shows the opinionated structure of a ViewModel's Contract, you are free to swap it out 
for any other classes you may already have defined. There is no requirement for any of these components to serializable 
in any way.

The contract is canonically a single top-level `object` with a name like `*Contract`, and it has 3 nested classes named 
`State`, `Inputs`, and `Events`.

```kotlin
object LoginScreenContract {
    data class State(
        val username: TextFieldValue,
        val password: TextFieldValue,
    )

    sealed class Inputs {
        data class UsernameChanged(val newValue: TextFieldValue) : Inputs()
        data class PasswordChanged(val newValue: TextFieldValue) : Inputs()
        object LoginButtonClicked : Inputs()
        object RegisterButtonClicked : Inputs()
    }

    sealed class Events {
        object NavigateToDashboard : Events()
        object NavigateToRegistration : Events()
    }
}
```

### State

The most important component of the MVI contract, and of the Ballast library, is the State. All the data in your UI that
changes meaningfully should be modeled in your State. States are persistent, held in-memory, and guaranteed to always 
exist through the StateFlow. You will typically observe a `StateFlow` of your ViewModel state, but you can also access 
it once as a snapshot at that point in time. How you build your UI and model your Inputs should be derived completely 
from how you model your State.

State is modeled as a Kotlin immutable `data class`:

```kotlin
data class State(
    val loggingIn: Boolean = false,
    val username: TextFieldValue = TextFieldValue(),
    val password: TextFieldValue = TextFieldValue(),
)
```

Many articles on MVI suggest for using a `sealed class` to model UI state. However, experience has shown me that UI 
states are rarely so cleanly delineated between such discrete states; you're more likely to have the UI go through a 
range of mixed values and states as data is loaded, refreshed, or changed by the user. Additionally, a `sealed class` as 
your State is only capable of modeling a single feature, but real-world UIs commonly have many features that all must be 
modeled simultaneously.

For these reasons, Ballast's opinion is that the Contract's State class should be a `data class`. But `sealed classes`
work great as individual properties within that State!

### Inputs

Inputs are the core of how Ballast does all its processing. The "intent" a user has when interacting with the UI is
captured into an Input class, which is sent to the Ballast ViewModel and scheduled to be processed at some later point
in time.

Inputs are modeled as a Kotlin `sealed class`:

```kotlin
sealed class Inputs {
    data class UsernameChanged(val newValue: TextFieldValue) : Inputs()
    data class PasswordChanged(val newValue: TextFieldValue) : Inputs()
    object LoginButtonClicked : Inputs()
    object RegisterButtonClicked : Inputs()
}
```

A good rule of thumb is to avoid re-using any Inputs for more than 1 purpose. It should be entirely clear what an Input
will do to the State _without having to look at its implementation or the State_. If you are tempted to re-send the same
input to do 2 different things, it should just be 2 different Inputs.

### Events

A necessary feature of UI programming is to handle actions once, only once, and only at the appropriate time (such as
Navigation requests). The processing of these Events is typically tightly coupled to the UI framework itself and doesn't
make much sense to be modeled in the State because the request is not persistent. Ballast uses Events as a way to keep
the platform-specific event-handling logic out of the ViewModel while ensuring all the guarantees of one-off Events that
one would expect.

Like Inputs, Events are modeled as a Kotlin `sealed class`:

```kotlin
sealed class Events {
    object NavigateToDashboard : Events()
    object NavigateToRegistration : Events()
}
```

## Handlers

Everything in the Contract is entirely declarative, but at some point Ballast needs to _do something_ with everything in
the Contract. There are several elements of a complete Ballast ViewModel that get composed together to implement the
full MVI pattern.

### Input Handlers

All of Ballast's processing revolves, literally, around the Input Handler. It is the only place in the MVI loop that is
allowed to run arbitrary code, and it is based upon Kotlin Coroutines to allow the entire processor loop to run
asynchronously. Inputs that get sent to a ViewModel are placed into a queue, and the Input Handler pulls them out of the
queue one-at-a-time to be processed.

And InputHandler is a class which implements the `InputHandler` interface. The `InputHandler.handleInput()` callback
receives a generic `Input` which should get split out into its sealed subclasses with a `when` statement. The 
InputHandler will be provided to the ViewModel upon its creation.

```kotlin
import LoginScreenContract.*

class LoginScreenInputHandler : InputHandler<Inputs, Events, State> {
    override suspend fun InputHandlerScope<Inputs, Events, State>.handleInput(
        input: Inputs
    ) = when (input) {
        is UsernameChanged -> { }
        is PasswordChanged -> { }
        is LoginButtonClicked -> { }
        is RegisterButtonClicked -> { }
    }
}
```

The `InputHandlerScope` DSL is able to update the ViewModel State, post Events, start sideJobs, and call any other 
suspending functions within the Input queue.

### Event Handlers

The Event Handler works very similarly to the Input Handler, but should implement `EventHandler` instead. Events are 
sent from the Input Handler into a queue, and the EventHandler will pull them out of the queue to be processed
one-at-a-time. 

Inputs are sent from the UI into the ViewModel, and finally delivered to the Input Handler. The Event Handler is the 
exact opposite, handling Events sent from the ViewModel to the UI. But crucially, the ViewModel may live longer than the 
UI element it is associated with, and so the EventHandler may be attached and detached dynamically in response to the
UI element's own lifecycle. Events sent while the Event Handler is detached will be queued, and will only be delivered 
to the EventHandler once the UI is back in a valid lifecycle state.

```kotlin
import LoginScreenContract.*

class LoginScreenEventHandler : EventHandler<Inputs, Events, State> {
    override suspend fun EventHandlerScope<Inputs, Events, State>.handleEvent(
        event: Events
    ) = when (event) {
        is Events.Notification -> { }
    }
}
```

The `EventHandlerScope` DSL is able to post Inputs back into the queue.

### Side-jobs

Inputs are normally processed in a queue, one-at-a-time, but there are lots of great use-cases for concurrent work in
the MVI model. Side-jobs allow you to start coroutines that run in the "background" of your ViewModel, on the side of
the normal Input queue. These side-jobs are bound by the same lifecycle as the ViewModel, and can even collect from
infinite flows.

Unlike all other components in Ballast, Side-jobs are just part of the `InputHandlerScope` DSL. You call 
`sideJob()`, provide it with a `key` that is used to determine when to restart it, and run your code in the lambda.

```kotlin
override suspend fun InputHandlerScope<Inputs, Events, State>.handleInput(
    input: Inputs
) = when (input) {
    is InfiniteSideJob -> {
        sideJob("ShortSideJob") {
            infiniteFlow()
                .map { Inputs.SomeInputType() }
                .onEach { postInput(it) }
                .launchIn(this)
        }
    }
}
```

The `sideJob()` lambda's receiver DSL is able to post both Inputs and Events back to the ViewModel. It also includes
a snapshot of the State taken when the SideJob is started, and a flag to let you know if the sideJob is started 
for the first time or restarted.

## Configuration

The above sections outline the overall _usage_ of Ballast, but there are a few more useful features that can expand the
functionality of Ballast with its configuration. 

### Config Builder 

All ViewModels will require a `BallastViewModelConfiguration` provided when they're created where most of the 
configuration takes place, but some platform-specific ViewModel classes may need some additional configuration, too. A
BasicViewModel configuration looks like this, using the helpful `BallastViewModelConfiguration.Builder`:

```kotlin
public class ExampleViewModel(
    viewModelScope: CoroutineScope
) : BasicViewModel<Inputs, Events, State>(
    config = BallastViewModelConfiguration.Builder()
        .apply {
            // set configuration common to all ViewModels, if needed
        }
        .withViewModel(
            initialState = State(),
            inputHandler = ExampleInputHandler(),
            name = "Example"
        )
        .build(),
    eventHandler = ExampleEventHandler(),
    coroutineScope = viewModelScope,
)
```

### Interceptors

One of the primary features of Ballast, and indeed one of the biggest benefits of the MVI pattern in general, is it 
ability to decouple the _intent_ to do work from the actual processing of that work. Because of this separation, it 
makes it possible to intercept all the objects moving throughout the ViewModel and add a bunch of other really useful
functionality, without requiring any changes to the Contract or Processor code.

A basic Interceptor works like a [Decorator][1], being attached to the ViewModel without affecting any of the normal
processing behavior of the ViewModel. It receives `BallastNotifications` from the ViewModel to notify the status of 
every feature as it goes through the steps of processing, such as being queued, completed, or failed. Basic Interceptors 
are purely a read-only mechanism, and are not able to make any changes to the ViewModel.

```kotlin
public class CustomInterceptor<Inputs : Any, Events : Any, State : Any>(
) : BallastInterceptor<Inputs, Events, State> {

    override suspend fun onNotify(logger: BallastLogger, notification: BallastNotification<Inputs, Events, State>) {
        // do something
    }
}
```

More advanced Interceptors are given additional privileges and are able to push changes back to the ViewModel. Rather 
than being notified when something interesting happens, they are notified when the ViewModel starts up and are given 
direct access to the Notifications flow, as well as a way to send data directly back into the ViewModel's processing 
queue, for doing unique and privileged things like time-travel debugging. Advanced Interceptors are able to restore the
ViewModel state arbitrarily, and send Inputs back to the ViewModel for processing, both of which will be handled 
processed in the normal queue by the InputStrategy.

```kotlin
public class CustomInterceptor<Inputs : Any, Events : Any, State : Any>(
) : BallastInterceptor<Inputs, Events, State> {

    public fun BallastInterceptorScope<Inputs, Events, State>.start(
        notifications: Flow<BallastNotification<Inputs, Events, State>>,
    ) {
        launch(start = CoroutineStart.UNDISPATCHED) {
            notifications.collect {
                onNotify(logger, it)
            }
        }
    }
}
```

Ballast offers a number of useful Interceptors and modules to aid in debugging and monitoring your application, see
{{ 'Modules' | anchor }}.

### Input Strategy

Until now in this page, I've described the Ballast ViewModel's internals as a "queue" and they're processed 
"one-at-a-time", but that's not entirely accurate. More specifically, Inputs are buffered into a Kotlin Coroutine 
Channel, and Ballast offers an API for customizing exactly how the Inputs are read from that channel.

{% snippet 'inputStrategies' %}

### Logging

Ballast offers a simple logging API integrated throughout the library. An instance of `BallastLogger` installed in the 
`BallastViewModelConfiguration` is exposed through all interfaces where custom code is run, so you don't have to juggle
injecting Loggers and properly matching up tags amongst all the different classes that make up the Ballast ViewModel.

```kotlin
import LoginScreenContract.*

class LoginScreenInputHandler : InputHandler<Inputs, Events, State> {
    override suspend fun InputHandlerScope<Inputs, Events, State>.handleInput(
        input: Inputs
    ) = when (input) {
        is UsernameChanged -> { }
        is PasswordChanged -> { }
        is LoginButtonClicked -> { 
            logger.info("Attempting Logging In...")
            val loginSuccessful = attemptLogin()
            if(loginSuccessful) {
                logger.info("Login success")
            } else {
                logger.info("Login failed")
            }
        }
        is RegisterButtonClicked -> { }
    }
}
```

{% snippet 'loggers' %}

By default, only logs written directly to the logger will be displayed, but by installing the `LoggingInterceptor` into
the `BallastViewModelConfiguration` you'll get automatic logging of all activity within the ViewModel. This interceptor
maintains a list of all Inputs and a copy of the latest State, so it may consume large amounts of memory or write 
sensitive information to the logger, and as such should never be used in production.

[1]: https://en.wikipedia.org/wiki/Decorator_pattern
[2]: {{ 'Ballast Firebase' | link }}
[3]: {{ 'Ballast Repository' | link }}
