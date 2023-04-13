[usage-guide.md](usage-guide.md)---
---

# High-level Workflow

The general workflow for Ballast involves the following steps:

1) Define a Contract
2) Write the InputHandler
3) Write the EventHandler
4) Combine everything into a ViewModel
5) Inject the ViewModel to your UI and start using it

These steps are described in more depth [below](#Ballast-Workflow), and while this workflow does involve a bit of 
boilerplate the [Intellij plugin][8] can help you in quickly scaffolding out all of these classes.

## Ballast Workflow

This section goes more in-depth into the individual components needed for the full Ballast Workflow. For a quick, 
high-level listing of the classes needed, see [High-level Workflow](#high-level-workflow).

### Define a Contract

The first step for using Ballast on any screen is to define the Contract. The Contract provides a structure for what 
data will be changing in your screen (the State), and how you will be interacting with it (Inputs), which gives you a 
single place to go to understand everything you need to know about any given screen. By having a dedicated Contract, you
won't have any hidden or undocumented functionality that is difficult to reproduce. 

If you're using Ballast in a multiplatform project, the Contract should be in the `commonMain` sourceSet.

See more about defining your contract in {{ anchor(itemId = "Thinking in Ballast MVI", pageAnchorId = "UI Contract", title = "Thinking in Ballast MVI") }}.

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

### Write the InputHandler

After defining the contract, you should then write the InputHandler to process the Inputs as they are received. The
InputHandler is the class that will be talking to your Repository layer, so any necessary Repositories should be 
provided through the InputHandler's contructor 

If you're using Ballast in a multiplatform project, the InputHandler should be in the `commonMain` sourceSet.

See more about writing your InputHandler in {{ anchor(itemId = "Features", pageAnchorId = "Input Handlers", title = "Features") }}.

```kotlin
import LoginScreenContract.*

class LoginScreenInputHandler(
    private val loginRepository: LoginRepository,
) : InputHandler<Inputs, Events, State> {
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

### Connect to the Platform UI

The last step is to actually use Ballast to build out your interactive UI. This typically involves several steps that 
will all be specific to the target you're running Ballast on, but compared to the effort involved with the Contract and
InputHandler, are relatively simple. So even though there is some platform-specific functionality you'll need to write, 
you will still be sharing the majority of the business-logic code in your application.

If you are using Ballast in a multiplatform application, the following pieces will typically be defined in the 
platform-specific sourceSets rather than in `commonMain`.

#### ViewModel

The first step is to define the ViewModel class for each [platform][1]. This will vary slightly depending on which 
platform you target, so that the ViewModel integrates well with the platform's normal lifecycle. For example, on 
Android, you'll make your screen's ViewModel extend `AndroidViewModel`, which is an instance of 
`androidx.lifecycle.ViewModel` that can be provided via Hilt or Navigation-Compose. For platforms that don't have their
own specific ViewModel implementation, or for use-cases where you want to manually control the ViewModel's lifecycle 
through a `CoroutineScope`, you can use `BasicViewModel` as the base class.

All ViewModel implementations will look pretty similar, regardless of the base class used. You'll need to create a
`BallastViewModelConfiguration` and pass it to the base class's constructor, along with any additional parameters needed
for the specific implementation, if any (for example, the `CoroutineScope` of a `BasicViewModel`). This is easiest to 
do with `BallastViewModelConfiguration.Builder`, but you can also structure everything with Dependency Injection, too 
(see section below on [Dependency Injection](#dependency-injection)). The `BallastViewModelConfiguration.Builder` is 
where you will specify the InputHandler and initial State for the ViewModel, as well as providing other more generic
configuration such as loggers or interceptors.

Despite each platform's native ViewModel being named the same and looking very similar, you typically wouldn't define it
with `actual/expect` declarations in a multiplatform project because there's usually no need to share the ViewModel 
itself in common code, so it just creates unnecessary overhead. Furthermore, the base classes for each platform 
typically have different constructors, so it's difficult to provide an `actual/expect` that is actually useful in common 
code for simplifying any DI. It's best to just provide the ViewModel implementations from the platform-specific DI 
modules.

```kotlin
// androidMain/ui/login/LoginScreenViewModel.kt
class LoginScreenViewModel() : AndroidViewModel<
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
            inputHandler = LoginScreenInputHandler(),
            name = "LoginScreen",
        )
        .build()
)

// jsMain/ui/login/LoginScreenViewModel.kt
class LoginScreenViewModel(
    viewModelCoroutineScope: CoroutineScope
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
            inputHandler = LoginScreenInputHandler(),
            name = "LoginScreen",
        )
        .build(),
    eventHandler = LoginScreenEventHandler(),
    coroutineScope = viewModelCoroutineScope,
)
```

#### EventHandler

The next step is to define an `EventHandler` for your ViewModel. The implementation will look very similar to an 
InputHandler, except that it will typically need a different implementation on each platform for handling things like
navigation requests (though this may not always be the case if you have your routing/navigation implemented entirely in
common code).

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

You may have noticed from the example ViewModel code above that the `BasicViewModel` has you providing the EventHandler
directly in its constructor, while the `AndroidViewModel` does not. This is because EventHandlers are closely related to
the lifecycle of the ViewModel, but don't necessarily follow the exact same lifecycle. The EventHandler typically lives
as long as the screen is active, but the ViewModel itself may be retained across multiple times of the screen being 
stopped and started. 

For a `BasicViewModel`, the lifecycle of the Screen, ViewModel, and EventHandler are all the same, and they're all
controlled by the lifetime of the `CoroutineScope`. When moving to a new screen, the screen's `CoroutineScope` is 
cancelled, the ViewModel's processing is stopped, and the EventHandler detached. For this reason, the `EventHandler` is
provided through the `BasicViewModel`'s constructor, to make sure they all respect the same lifecycle.

But on Android, it is not possible to use Hilt to inject a ViewModel with anything that depends on the Activity, since a 
ViewModel lives longer than the Activity. Since the `EventHandler` is commonly used for handling Navigation requests, 
and navigation is done by the activity through `Activity.startActivity()` or `findNavController().navigate()`, it is 
impossible to inject the `EventHandler` directly into the ViewModel, but instead it must be attached dynamically after
the ViewModel has been injected. See the [Android platform page][5] for specific instructions.

#### UI

The final piece of the Ballast puzzle is actually defining your UI given the Ballast State. This typically involves 
creating or accessing an instance of your ViewModel and observing its State as a `StateFlow` with 
`viewModel.observeStates()`. On each emission of that StateFlow, you will update the entire UI of the screen with the 
new State, as per for the platform-specific requirements. 

On platforms that require the native programming language to use rather than Kotlin (SwiftUI, for example), there may be
some boilerplate needed to wrap the Kotlin coroutines and `StateFlow` into something that the platform's native code can
integrate with. But on Android, and using Compose for Desktop or Web, this is easily done in Kotlin. See each 
[platform's][1] instructions for how to connect to the actual UI toolkit.
