---
---

# {{ page.title }}

There is no special support required to use Ballast in native Android applications. You can use the normal 
`BasicViewModel` as your ViewModel implementation if you need to control its lifecycle with a custom `CoroutineScope`, 
such as when scoping the ViewModel to a Compose function. The `BasicViewModel` is unrelated to 
`androidx.lifecycle.ViewModel`, and thus it cannot be provided from any of the normal Android ViewModel mechanisms, but
should be managed entirely custom.

Ballast does offer `AndroidViewModel`, which is a subclass of `androidx.lifecycle.ViewModel` and uses the 
`viewModelScope` to control the ViewModel's lifecycle. Subclasses of `AndroidViewModel` can be scoped to Activities, 
Fragments, or NavGraphs as usual, and also work with [Hilt's `@AndroidViewModel` injection][1]. There is also a 
`AndroidBallastRepository` which extends `androidx.lifecycle.ViewModel` as the Android-specific analog of 
`BallastRepository` from the {{ 'Ballast Repository' | anchor }} module.

Unlike `BasicViewModel`, a ViewModel intentionally does not have access to the Activity or Fragment it is typically
associated with. Thus, it is not possible to provide the `EventHandler` directly an instance of `AndroidViewModel` with
Hilt. It will have to be attached dynamically with `vm.attachEventHandler()` after creation (typically during a 
Fragment's `onViewCreated()` callback). 

## Example

```kotlin
@AndroidEntryPoint
class ExampleFragment : ComposeFragment() {

    @Inject
    lateinit var eventHandler: ExampleEventHandler.Factory

    private val viewModel: ExampleViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    val uiState by viewModel.observeStates().collectAsState()
                    ExampleContent(uiState) {
                        viewModel.trySend(it)
                    }
                }
            }
        }
    }

    // When using Compose with the MVI pattern, we need to connect the event handler to this
    // Fragment instance manually, since Dagger/Hilt are unable to wire up the dependencies
    // automatically due to how ViewModels are scoped/created.
    //
    // If we need to load data on the initial screen load, it should be posted as an Input to the
    // ViewModel here.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.attachEventHandler(
            this,
            eventHandler.create(this, findNavController())
        )
        viewModel.trySend(ExampleContract.Inputs.Initialize)
    }

    @Composable
    fun ExampleContent(
        uiState: ExampleContract.State = ExampleContract.State(),
        postInput: (ExampleContract.Inputs) -> Unit = {},
    ) {
        // ...
    }
}
```

[1]: https://dagger.dev/hilt/view-model.html
