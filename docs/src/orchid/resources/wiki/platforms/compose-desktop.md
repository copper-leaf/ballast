---
---

# {{ page.title }}

There is no special support needed to use Ballast in [Compose Desktop][1] or other [Kotlin/JVM][2] applications. You 
will use the `BasicViewModel` as your ViewModel implementation, and connect it to a custom `CoroutineScope` to control 
its lifecycle.

## Example

```kotlin
fun main() {
    singleWindowApplication {
        MaterialTheme {
            val applicationCoroutineScope = rememberCoroutineScope()
            val viewModel = remember(applicationCoroutineScope) { ExampleViewModel(applicationCoroutineScope) }
            val uiState by viewModel.observeStates().collectAsState()
            Content(uiState) {
                viewModel.trySend(it)
            }
        }
    }
}

@Composable
fun ExampleContent(
    uiState: ExampleContract.State = ExampleContract.State(),
    postInput: (ExampleContract.Inputs) -> Unit = {},
) {
    // ...
}
```

[1]: https://www.jetbrains.com/lp/compose-mpp/
[2]: https://kotlinlang.org/docs/jvm-get-started.html
