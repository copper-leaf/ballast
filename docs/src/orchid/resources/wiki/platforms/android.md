---
---

# {{ page.title }}

There is no special support required to use Ballast in native Android applications. It works with both Compose and 
traditional XML View-based screens, as well as Activity-, Fragment-, or pure-Compose-based screens/navigation.

## Usage

### AndroidViewModel

Ballast offers `AndroidViewModel`, which is a subclass of `androidx.lifecycle.ViewModel` and uses the
`viewModelScope` to control the ViewModel's lifecycle. Subclasses of `AndroidViewModel` can be scoped to Activities,
Fragments, or NavGraphs as usual, and also work with [Hilt's `@AndroidViewModel` injection][1]. There is also a
`AndroidBallastRepository` which extends `androidx.lifecycle.ViewModel` as the Android-specific analog of
`BallastRepository` from the {{ 'Ballast Repository' | anchor }} module.

An `AndroidViewModel` intentionally does not have access to the Activity or Fragment it is typically associated with 
when created or during Hilt injection, as it lives longer than the associated Activity/Fragment. Thus, it is not 
possible to provide the `EventHandler` directly an instance of `AndroidViewModel` with Hilt. It will have to be attached
dynamically with `vm.attachEventHandler()` after creation. In a View-based screen, this would be attached in a 
Fragment's `onViewCreated()` callback or an Activity's `onStart()` or `onResume()` callbacks. In either case, the 
EventHandler itself will only be active during the `RESUMED` state, and collected [safely with `repeatOnLifecycle`][2]. 
Within Compose, you can call `vm.attachEventHandler()` within a `LaunchedEffect` to handle events on the coroutineScope
of a particular Composable function.

### Other

if you need to control its lifecycle with another `CoroutineScope` (such as when scoping the ViewModel to a Compose 
function), you can use the normal `BasicViewModel` as your ViewModel implementation. The `BasicViewModel` is unrelated to 
`androidx.lifecycle.ViewModel`, and thus it cannot be provided from any of the normal Android ViewModel mechanisms, but
gives you more flexibility over the lifetime of the ViewModel.

## Examples

### XML Views

```kotlin
@AndroidEntryPoint
class ExampleFragment : ComposeFragment() {

    @Inject
    lateinit var eventHandler: ExampleEventHandler.Factory

    private val viewModel: ExampleViewModel by viewModels()
    
    private var binding: FragmentExampleBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentExampleBinding
            .inflate(inflater, container, false)
            .also { binding = it }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // events are sent back to the screen during the Fragment's Lifecycle RESUMED state
        viewModel.attachEventHandlerOnLifecycle(
            this,
            eventHandler.create(this, findNavController()),
        )
        
        // Collect the state on the Fragment's Lifecycle RESUMED state, updating the entire UI with each change
        vm.observeStatesOnLifecycle(this) { state -> 
            binding?.updateWithState(state) { viewModel.trySend(it) } 
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun FragmentExampleBinding.updateWithState(
        state: ExampleContract.State,
        postInput: (ExampleContract.Inputs) -> Unit
    ) {
        tvCounter.text = "${state.count}"

        btnDec.setOnClickListener { postInput(ExampleContract.Inputs.Decrement(1)) }
        btnInc.setOnClickListener { postInput(ExampleContract.Inputs.Increment(1)) }
    }
}
```

### Compose

If you're writing a pure Compose Android application, see the [Compose][3] page for integration with using 
`BasicViewModel`. But if you're developing a hybrid app which uses Activities or Fragments for navigation and Compose
views within them, you'll probably want to use `AndroidViewModel` and inject the ViewModels with Hilt, and the 
integration process will need to handle some additional features like dynamically attaching/removing the EventHandler.

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
                    
                    LaunchedEffect(viewModel, eventHandler) {
                        viewModel.attachEventHandler(
                            this,
                            eventHandler.create(this, findNavController())
                        )
                        viewModel.trySend(ExampleContract.Inputs.Initialize)
                    }
                    
                    ExampleContent(uiState) {
                        viewModel.trySend(it)
                    }
                }
            }
        }
    }

    @Composable
    fun ExampleContent(
        uiState: ExampleContract.State,
        postInput: (ExampleContract.Inputs) -> Unit,
    ) {
        // ...
    }
}
```

[1]: https://dagger.dev/hilt/view-model.html
[2]: https://developer.android.com/reference/kotlin/androidx/lifecycle/package-summary#(androidx.lifecycle.Lifecycle).repeatOnLifecycle(androidx.lifecycle.Lifecycle.State,kotlin.coroutines.SuspendFunction1)
[3]: {{ 'Compose' | link }}
