# Getting Started

This guide walks through building a complete Ballast screen from scratch, using a simple counter as the example. By
the end you will have a working Ballast MVI ViewModel wired into a Compose UI. For deeper explanations of each concept,
see the [Feature Overview](feature-overview.md).

Ballast builds on top of standard Kotlin constructs, and uses Coroutines heavily, both in its internals and in its 
surface API. You should be fairly comfortable with Coroutines concepts like structured concurrency and Flows, before
getting started with Ballast, and knowledge of Channels can help you understand the kinds of safety guarantees Ballast
brings to your application logic.

## Installation

Add `ballast-core` to your dependencies. It brings in everything needed to create and run ViewModels.

```kotlin
repositories {
    mavenCentral()
}

// multiplatform
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-core:{{ballastVersion}}")
            }
        }
    }
}

// JVM/Android only
dependencies {
    implementation("io.github.copper-leaf:ballast-core:{{ballastVersion}}")
}
```

## Step 1: Define the Contract

The Contract is a single object that declares everything that can happen on a screen: what data is shown (State),
what the user can do (Inputs), and what one-off effects need to be handled by the UI (Events). It is entirely
plain Kotlin — no Ballast types required.

```kotlin
object CounterContract {
    data class State(
        val count: Int = 0,
    )

    sealed interface Inputs {
        data object Increment : Inputs
        data object Decrement : Inputs
        data object ResetRequested : Inputs
    }

    sealed interface Events {
        data object ConfirmReset : Events
    }
}
```

**State** is an immutable `data class` so it can be updated with `.copy()`. Model everything the UI needs to render as a
property here — if it is not in State, the UI cannot see it. The State is stored in a `StateFlow` within the ViewModel, 
which allows updates to the State to be pushed out to the UI automatically and atomically.

**Inputs** are the user's intent, modeled as a `sealed interface`. Each Input should have one clear purpose; if
you need the same button to do two different things in different situations, that should be two different Inputs. Inputs
are sent from the UI and buffered through a `Channel` so they are processed one-at-a-time in the order they were sent 
in, and are typically processed on the Default dispatcher.

**Events** are one-shot notifications sent from the ViewModel out to the UI — navigation requests, showing a
dialog, etc. Events are delivered at-most-once and are not kept in State. Events are also buffered through a `Channel` 
from the VM to the UI, and are typically delivered on the Main dispatcher.

## Step 2: Write the InputHandler

The InputHandler is the only place in the MVI loop that runs arbitrary code. It receives each Input from the `Channel` 
queue one at a time and uses the `InputHandlerScope` DSL to update State, post Events, or start background side jobs.

Inputs are strongly-typed, and through the exhaustive type-checking of their parent `sealed class`, a `when` block that
returns `Unit` ensures each subtype is handled properly.

```kotlin
import CounterContract.*

class CounterInputHandler : InputHandler<Inputs, Events, State> {
    override suspend fun InputHandlerScope<Inputs, Events, State>.handleInput(
        input: Inputs,
    ) = when (input) {
        is Inputs.Increment -> updateState { copy(count = count + 1) }
        is Inputs.Decrement -> updateState { copy(count = count - 1) }
        is Inputs.ResetRequested -> postEvent(Events.ConfirmReset)
    }
}
```

Keep the InputHandler free of platform-specific code — it belongs in `commonMain` if you are targeting multiple
platforms.

## Step 3: Write the EventHandler

The EventHandler receives Events from the ViewModel and handles any platform-specific side effects. It can be
attached and detached dynamically as the UI's lifecycle changes; Events sent while it is detached are queued and
delivered when it reattaches.

```kotlin
import CounterContract.*

class CounterEventHandler(
    private val onConfirmReset: () -> Unit,
) : EventHandler<Inputs, Events, State> {
    override suspend fun EventHandlerScope<Inputs, Events, State>.handleEvent(
        event: Events,
    ) = when (event) {
        is Events.ConfirmReset -> onConfirmReset()
    }
}
```

## Step 4: Input Strategies — Set FIFO explicitly

Before creating the ViewModel you need to choose an **Input Strategy**, which controls what happens when a new
Input arrives while the previous one is still processing:

- **`FifoInputStrategy`** (first-in, first-out): Inputs are processed in order, one at a time. If a new Input
  arrives while one is running, it waits its turn. Corresponds to `Flow.collect { }`. This is the most
  predictable strategy and the one you should start with.

- **`LifoInputStrategy`** (last-in, first-out): If a new Input arrives while one is running, the running Input
  is *cancelled* so the newer one can start immediately. Corresponds to `Flow.collectLatest { }`. Useful for
  highly responsive UIs (e.g. live search), but easy to get wrong. It's not uncommon for a user action or a background
  task to send an Input and cancel running work unexpectedly. 

> **Warning:** For historical reasons, `LifoInputStrategy` is the default. Always set the strategy explicitly so
> your app does not silently change behaviour in a future Ballast release that switches the default.
> In most situations, `FifoInputStrategy` should be your preferred InputStrategy since it will work the most predictably
> with the fewest surprises, but you must make sure to choose it maually.

## Step 5: Create the ViewModel

The ViewModel assembles everything together using `BallastViewModelConfiguration.Builder`. Since this example
targets a general Kotlin/Compose environment (not Android or any other platform, specifically), it extends 
`BasicViewModel`, whose lifecycle is controlled by a `CoroutineScope` you provide.

```kotlin
class CounterViewModel(
    coroutineScope: CoroutineScope,
    eventHandler: CounterEventHandler,
) : BasicViewModel<
    CounterContract.Inputs,
    CounterContract.Events,
    CounterContract.State,
>(
    config = BallastViewModelConfiguration.Builder()
        .apply {
            // Always set the input strategy explicitly
            inputStrategy = FifoInputStrategy.typed()

            // LoggingInterceptor prints all ViewModel activity to the logger.
            // It logs with the standard .toString() representation of the 
            // Inputs and States which may contain sensitive information, so it 
            // should only be used in Debug builds. 
            this += LoggingInterceptor()
            logger = { PrintlnLogger(it) }
        }
        .withViewModel(
            initialState = CounterContract.State(),
            inputHandler = CounterInputHandler(),
            name = "Counter",
        )
        .build(),
    eventHandler = eventHandler,
    coroutineScope = coroutineScope,
)
```

`LoggingInterceptor` is the most useful interceptor to add from the start. It automatically logs every Input
received, every State change, and every Event or Side Job dispatched, giving you a clear picture of what is
happening inside the ViewModel without any manual logging in your handlers.

## Step 6: Wire it up with a simple injector

A `BasicViewModel` needs a `CoroutineScope` to define its lifetime. The scope should live as long as the screen
that owns the ViewModel, and be cancelled when that screen is destroyed. The simplest way to manage this without
a DI framework is a hand-written injector object which receives the coroutineScope directly from the UI component.

```kotlin
object AppInjector {
    fun counterViewModel(coroutineScope: CoroutineScope): CounterViewModel {
        return CounterViewModel(
            coroutineScope = screenScope,
            eventHandler = CounterEventHandler(
                onConfirmReset = {
                    // show a dialog, navigate, etc.
                },
            ),
        )
    }
}
```

> **Note:** A hand-written injector like this is fine for getting started, but a proper DI framework gives you
> automatic scope management and easier testing. [Koin](https://insert-koin.io/) and
> [Metro](https://github.com/ZacSweers/metro) both work well with Ballast. With either framework you would
> typically bind the ViewModel to a *screen scope* (scoped to the navigation back-stack entry) rather than the
> application or composition scope.

## Step 7: Connect to the Compose UI

Observe the ViewModel's `StateFlow` with `collectAsState()` and send Inputs from event handlers. The entire UI
is a pure function of State — never store copies of State properties in local Compose state.

```kotlin
@Composable
fun CounterScreen() {
    val coroutineScope = rememberCoroutineScope()
    val viewModel: CounterViewModel = remember { AppInjector.counterViewModel(coroutineScope) }
    val state by viewModel.observeStates().collectAsState()

    CounterContent(
        state = state,
        postInput = { viewModel.trySend(it) },
    )
}

@Composable
fun CounterContent(
    state: CounterContract.State,
    postInput: (CounterContract.Inputs) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Count: ${state.count}")

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { postInput(CounterContract.Inputs.Decrement) }) {
                Text("−")
            }
            Button(onClick = { postInput(CounterContract.Inputs.Increment) }) {
                Text("+")
            }
        }

        TextButton(onClick = { postInput(CounterContract.Inputs.ResetRequested) }) {
            Text("Reset")
        }
    }
}
```

Splitting the screen into a `*Screen` composable (which accesses the ViewModel) and a stateless `*Content` composable
(which only takes State and a callback) is a useful pattern — the Content composable can be previewed and tested
without a real ViewModel.

## What's next

- **Platforms**: For Android, use `AndroidViewModel` instead of `BasicViewModel` — see
  [ballast-viewmodel](../ballast-viewmodel/).
- **Debugging**: Add [ballast-debugger-client](../ballast-debugger-client/) to inspect your ViewModels live in
  the IDE using the [Ballast IntelliJ Plugin](../ballast-idea-plugin/).
- **Navigation**: See [ballast-navigation](../ballast-navigation/) for routing.
- **Persistence**: See [ballast-saved-state](../ballast-saved-state/) for restoring State across process death.
- **Testing**: See [ballast-test](../ballast-test/) for testing InputHandlers.
- **Deep concepts**: See [Thinking in Ballast MVI](mental-model.md) for the full mental model behind the design.
