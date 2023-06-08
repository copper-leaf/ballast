---
---

# {{ page.title }}

There is no special support needed to use Ballast in Compose for browser applications or other [Kotlin/JS][1] 
applications. The general idea is the same whether you're using [Compose Multiplatform][2] for canvas-based Web apps or
targeting browser DOM with [Compose HTML][3] You will use the `BasicViewModel` as your ViewModel implementation, and 
connect it to a custom `CoroutineScope` to control its lifecycle.

All {{ 'Examples' | anchor }} are using Ballast in Kotlin/JS with Compose HTML, so you can get a feel for what you can 
do with Ballast without leaving your browser.

## Example

```kotlin
fun main() {
    renderComposable(root = "root") {
        val applicationCoroutineScope = rememberCoroutineScope()
        val viewModel = remember(applicationCoroutineScope) { ExampleViewModel(applicationCoroutineScope) }
        val uiState by viewModel.observeStates().collectAsState()
        Content(uiState) {
            viewModel.trySend(it)
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

[1]: https://kotlinlang.org/docs/js-overview.html
[2]: https://github.com/JetBrains/compose-multiplatform/#web
[3]: https://github.com/JetBrains/compose-multiplatform/#compose-html
