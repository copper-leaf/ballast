# Ballast ViewModel

## Overview

Default implementations of `BallastViewModel`, as the base class your own ViewModels should use or extend.

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

### BasicViewModel

`BasicViewModel` is generic ViewModel for Kotlin targets that don't have their own platform-specific ViewModel, or for
anywhere you want to manually control the lifecycle of the ViewModel. `BasicViewModel`'s lifecycle is controlled by a
`coroutineScope` provided to it upon creation. When the scope gets cancelled, the ViewModel gets closed and can not be
used again.

This is the recommended choice for Compose Multiplatform applications, as it works on all supported platforms and you
can attach a ViewModel to an arbitrary point in the composition with `rememberCoroutineScope()`. Typically, you would
attach the ViewModel to the root composable of a Screen, collect its state, and pass the VM State and a `postInput`
lambda to a stateless version of the Screen composable.

A `BasicViewModel` attaches the EventHandler directly in the constructor, so it is running and collecting Events as long
as the ViewModel itself is active.

```kotlin
class ExampleViewModel(coroutineScope: CoroutineScope) : BasicViewModel<
        ExampleContract.Inputs,
        ExampleContract.Events,
        ExampleContract.State
        >(
    coroutineScope = coroutineScope,
    config = BallastViewModelConfiguration.Builder()
        .withViewModel(
            initialState = ExampleContract.State(),
            inputHandler = ExampleInputHandler(),
            name = "Example"
        )
        .build(),
    eventHandler = eventHandler { },
)

// stateful Screen function, with state managed by the ExampleViewModel
@Composable
fun ExampleScreen() {
    val viewModelCoroutineScope = rememberCoroutineScope()
    val vm: ExampleViewModel = remember(viewModelCoroutineScope) {
        ExampleViewModel(viewModelCoroutineScope)
    }

    // collect the VM state and call the stateless function
    val uiState by vm.observeStates().collectAsState()
    ExampleScreen(uiState) { vm.trySend(it) }
}

// stateless Screen function
@Composable
fun ExampleScreen(
    uiState: ExampleContract.State,
    postInput: (ExampleContract.Inputs)->Unit
) {
    // ...
}
```

### AndroidViewModel

The `AndroidViewModel` is a subclass of `androidx.lifecycle.ViewModel`, which allows it to be retained for longer
durations, and shared throughout your app via Dependency Injection. It is only supported on Android targets.

Since AndroidViewModels may be retained and active while the app or screen it supplies is not in the foreground, the
EventHandler should be attached dynamically when the ViewModel's corresponding UI component is brought back into the
foreground. It also contains helper functions for collecting the State on a valid Lifecycle state.

**Compose UI Example with Koin injection**

```kotlin
class ExampleViewModel() : AndroidViewModel<
        ExampleContract.Inputs,
        ExampleContract.Events,
        ExampleContract.State
        >(
    coroutineScope = MainScope(), // not necessary, but recommended so you can inject Dispatchers for testing
    config = BallastViewModelConfiguration.Builder()
        .withViewModel(
            initialState = ExampleContract.State(),
            inputHandler = ExampleInputHandler(),
            name = "Example"
        )
        .build(),
)

// stateful Screen function, with state managed by the ExampleViewModel and injected by Koin
@Composable
fun ExampleScreen(vm: ExampleViewModel = koinViewModel()) {
    // collect the VM state and call the stateless function
    val uiState by vm.observeStates().collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(vm, lifecycleOwner) {
        viewModel.attachEventHandlerOnLifecycle(this, ExampleEventHandler())
    }

    ExampleScreen(uiState) { vm.trySend(it) }
}

// stateless Screen function
@Composable
fun ExampleScreen(
    uiState: ExampleContract.State,
    postInput: (ExampleContract.Inputs)->Unit
) {
    // ...
}
```

**XML UI Example with Koin injection**

```kotlin
class ExampleViewModel() : AndroidViewModel<
        ExampleContract.Inputs,
        ExampleContract.Events,
        ExampleContract.State
        >(
    coroutineScope = MainScope(), // not necessary, but recommended so you can inject Dispatchers for testing
    config = BallastViewModelConfiguration.Builder()
        .withViewModel(
            initialState = ExampleContract.State(),
            inputHandler = ExampleInputHandler(),
            name = "Example"
        )
        .build(),
)

class ExampleActivity : AppCompatActivity() {

    // Lazy inject ViewModel
    val detailViewModel: ExampleViewModel by viewModel()
    private var binding: ExampleActivityBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(
            ExampleActivityBinding
                .inflate(layoutInflater, null, false)
                .also { binding = it }
                .root
        )

        // Collect the state and react to events during the Fragment's Lifecycle RESUMED state
        vm.runOnLifecycle(this, ExampleEventHandler(this)) { state ->
            binding?.updateWithState(state) { event -> vm.trySend(event) }
        }
    }

    private fun ExampleActivityBinding.updateWithState(
        state: ExampleContract.State,
        postInput: (ExampleContract.Inputs) -> Unit
    ) {
        // update XML UI and re-register listeners
    }
}
```

### IosViewModel

A custom ViewModel that can be integrated with Combine Publishers for SwiftUI. This is not a recommended approach as it
is difficult to bridge Kotlin and SwiftUI directly at this layer, and this ViewModel was never tested or used
thoroughly. Either use a fully-Kotlin UI with Compose Multiplatform and Ballast ViewModels, or let the SwiftUI use its
own ViewModels and Ui state management, paired with Kotlin Multiplatform for the Domain/Data layers if your application.

## Installation

```kotlin
repositories {
    mavenCentral()
}

// for plain JVM or Android projects
dependencies {
    implementation("io.github.copper-leaf:ballast-viewmodel:{{ballastVersion}}")
}

// for multiplatform projects
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-viewmodel:{{ballastVersion}}")
            }
        }
    }
}
```
