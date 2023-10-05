---
---

# {{ page.title }}

There is no special support needed to use Ballast in [Compose][1] applications, and it works on Android, Desktop, iOS, 
and JS targets (WASM support coming soon). For JS, it can also be used with both Canvas and [Compose HTML][3] 
applications. The integration process for all of these use-cases is the same. 

## Example

A good pattern for defining the Compose UI with Ballast State Management is to create a single `object` with two main
functions for the UI. One which is fully stateless, taking parameters of only the VM state and a `postInput` callback, 
and another which creates and manages the Ballast ViewModel that internally calls the stateless version.

```kotlin
public object ExampleUi {

    @Composable
    public fun Content() {
        // this can 
        val viewModelCoroutineScope = rememberCoroutineScope()
        val vm: ExampleViewModel = remember(viewModelCoroutineScope) {
            BasicViewModel(
                coroutineScope = viewModelCoroutineScope,
                config = BallastViewModelConfiguration.Builder()
                    .withViewModel(
                        initialState = ExampleContract.State(),
                        inputHandler = ExampleInputHandler(),
                    )
                    .build(),
                eventHandler = ExampleEventHandler(),
            )
        }
        
        
        // collect the VM state and call the stateless Content() function
        val uiState by vm.observeStates().collectAsState()

        Content(uiState) { vm.trySend(it) }
    }

    @Composable
    public fun Content(
        uiState: ExampleContract.State,
        postInput: (ExampleContract.Inputs) -> Unit,
    ) {
        // ...
    }
}
```

[1]: https://www.jetbrains.com/lp/compose-multiplatform/
[3]: https://github.com/JetBrains/compose-multiplatform/#compose-html
