---
---

# {{ page.title }}

There is no special support needed to use Ballast in [Compose Web][1] or other [Kotlin/JS][2] applications. You will use
the `BasicViewModel` as your ViewModel implementation, and connect it to a custom `CoroutineScope` to control its 
lifecycle.

All {{ 'Examples' | anchor }} are using Ballast in Kotlin/JS, so you can get a feel for what you can do with Ballast
without leaving your browser.

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

[1]: https://compose-web.ui.pages.jetbrains.team/
[2]: https://kotlinlang.org/docs/js-overview.html
