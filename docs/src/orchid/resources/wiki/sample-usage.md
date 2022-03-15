---
---

# Sample Usage

This past will walk you though the basics of modeling a single screen in your application with the Ballast MVI
architecture. This sample will build a fairly standard Login screen, with the following features:

- The user logs in with a username and password
- The user may choose to remember their username for subsequent sessions with a checkbox
- The "Login" button is disabled until both the username and password have been entered (only validating that they are
  not empty)
- If the user opted to remember their username from the previous session, the next session should pre-populate the
  "username" field with the username that was submitted from the last successful login. Additionally, the checkbox to
  remember username should still be checked.

Notice how all the requirements for this screen are related to the user of the application. In Ballast, we define a
"Contract" class to capture all of these user-focused requirements, but we leave the implementation details of how
exactly they work to other classes.

Conventionally, the contract is a top-level `object` with nested classes named `State`, `Inputs`, and `Events`:

```kotlin
object LoginContract {
  data class State(
    val submitting: Boolean = false,
    val rememberUsername: Boolean = false,
    val username: String = "",
    val password: String = "",
  )

  sealed class Inputs {
    object Initialize : Inputs()

    data class UsernameUpdated(val value: String) : Inputs()
    data class PasswordUpdated(val value: String) : Inputs()
    object RememberUsernameToggled : Inputs()

    object LoginClicked : Inputs()
  }

  sealed class Events {
    object GoToDashboard : Events()
    data class ShowSnackbar(val message: String) : Events()
  }
}
```

The `State` will be sent to the UI with every change, and the UI generates `Inputs` to send to the `ViewModel` which are
processed by the `InputHandler`.

The `InputHandler` receives the generic `Input` and uses a `when` statement to break it back out into the specific type.
Because `Inputs` are defined as a `sealed class`, we can be sure this `when` statement will handle every possible
`Input` that could be sent to the `ViewModel`.

The `InputHandler` is also given a special `InputHandlerScope`, which provides a dedicated DSL for processing the Input
and updating the state, sending one-time events back to the UI, and other functionality. The InputHandler is also a
`suspend` function, so it is free to make API calls, such as through a `Repository`, and connect to other data sources.
Also notice how these data sources are not exposed through the Contract; they are simply implementation details of the
`InputHandler`, not something the user actually knows or cares about.

```kotlin
class LoginInputHandler(
  private val loginRepository: LoginRepository,
) : InputHandler<
        LoginContract.Inputs,
        LoginContract.Events,
        LoginContract.State> {

  override suspend fun InputHandlerScope<
          LoginContract.Inputs,
          LoginContract.Events,
          LoginContract.State>.handleInput(
    input: LoginContract.Inputs
  ) = when (input) {
    is LoginContract.Inputs.Initialize -> {
      val currentState = updateStateAndGet { it.copy(rememberUsername = loginRepository.rememberUsername) }

      if (currentState.rememberUsername) {
        updateState { it.copy(username = loginRepository.savedUsername) }
      }

      Unit
    }
    is LoginContract.Inputs.UsernameUpdated -> {
      updateState { it.copy(username = input.value) }
    }
    is LoginContract.Inputs.PasswordUpdated -> {
      updateState { it.copy(password = input.value) }
    }
    is LoginContract.Inputs.RememberUsernameToggled -> {
      val currentState = updateStateAndGet { it.copy(rememberUsername = !it.rememberUsername) }
      loginRepository.rememberUsername = currentState.rememberUsername
    }
    is LoginContract.Inputs.LoginClicked -> {
      val currentState = updateStateAndGet { it.copy(submitting = true) }

      val loggedInSuccessfully = loginRepository.tryLogin(
        currentState.username,
        currentState.password,
      )

      if (loggedInSuccessfully) {
        if (currentState.rememberUsername) {
          loginRepository.savedUsername = currentState.username
        } else {
          loginRepository.savedUsername = ""
        }

        postEvent(LoginContract.Events.GoToDashboard)
      } else {
        postEvent(LoginContract.Events.ShowSnackbar("Username or password incorrect"))
      }

      updateState { it.copy(submitting = false) }
    }
  }
}
```

Up until this point, all the code we've written is pure-Kotlin, and is suitable to be used as the shared business-logic 
of a KMM application, for example. The Contract and InputHandler are definitely the largest chunks of code in the 
Ballast pattern, but there are a few things we'll need to tie it all together in a complete application, starting with
the actual screen, such as a Fragment or Activity in Android.

The Fragment is the main entry-point to the Ballast library, and there are many ways to set it up and use it, depending 
on your project setup. The snippet below shows the most "vanilla" setup to demonstrate how all the pieces are ultimately
assembled and used, but a real application would probably do things like:

- use Dagger and Hilt for injecting all the components, rather than creating them manually
- use Compose for displaying the State in the UI or be more intelligent about applying the State to the normal XML views

```kotlin
class LoginFragment : Fragment() {

  var binding: LoginBinding? = null

  private val viewModel: LoginViewModel by viewModels {
    object : ViewModelProvider.Factory {
      override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if(modelClass == LoginViewModel::class.java) {
          LoginViewModel(
            LoginInputHandler(
              LoginRepository()
            )
          ) as T
        } else {
          error("unknown ViewModel class: $modelClass")
        }
      }
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return LoginBinding.inflate(inflater, null, false)
      .apply {
        // wire up the View to sent Inputs to the ViewModel
        etUsername.doAfterTextChanged {
          viewModel.trySend(LoginContract.Inputs.UsernameUpdated(it.toString()))
        }
        etPassword.doAfterTextChanged {
          viewModel.trySend(LoginContract.Inputs.PasswordUpdated(it.toString()))
        }
        cbRememberMe.setOnClickListener {
          viewModel.trySend(LoginContract.Inputs.RememberUsernameToggled)
        }
        btnLogin.setOnClickListener {
          viewModel.trySend(LoginContract.Inputs.LoginClicked)
        }
      }
      .also { binding = it }
      .root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    lifecycleScope.launchWhenCreated {
      repeatOnLifecycle(Lifecycle.State.CREATED) {
        // observe state changes, and apply them to the UI
        viewModel.observeStates().collect { state ->
          binding?.apply {
            etUsername.setText(state.username)
            etPassword.setText(state.password)
            cbRememberMe.isChecked = state.rememberUsername
            btnLogin.isEnabled = state.username.isNotBlank() && state.password.isNotBlank()
          }
        }
      }
    }

    // handle one-time Events
    viewModel.attachEventHandler(
      this,
      LoginEventHandler(this)
    )

    // Initialize the ViewModel
    viewModel.trySend(LoginContract.Inputs.Initialize)
  }
}
```

Living within that Fragment, is the `ViewModel` and `EventHandler`. The ViewModel is the core container which implements
all core logic within Ballast, and the EventHandler processes any 1-time events sent from the ViewModel. The UI 
dispatches all Inputs into the ViewModel, which forwards them to the InputHandler.

```kotlin
class LoginViewModel(
  inputHandler: LoginInputHandler,
) : AndroidViewModel<
        LoginContract.Inputs,
        LoginContract.Events,
        LoginContract.State>(
  config = BallastViewModelConfiguration.Builder()
    .apply {
      this += LoggingInterceptor()
    }
    .forViewModel(
      initialState = LoginContract.State(),
      inputHandler = inputHandler,
      name = "Login",
    )
)
```

```kotlin
class LoginEventHandler(
  val fragment: Fragment
) : EventHandler<
        LoginContract.Inputs,
        LoginContract.Events,
        LoginContract.State> {

  override suspend fun EventHandlerScope<LoginContract.Inputs, LoginContract.Events, LoginContract.State>.handleEvent(
    event: LoginContract.Events
  ) = when (event) {
    is LoginContract.Events.GoToDashboard -> {}
    is LoginContract.Events.ShowSnackbar -> {}
  }
}
```

These last 3 classes, the Fragment, Event Handler, and ViewModel, are all directly related to the platform you are using
Ballast on. To use Ballast on another target, or even to share your KMM app's business logic across multiple apps, you 
would only need to reimplement the Event Handler for you platform and choose the appropriate base ViewModel class, and 
hook it into the platform's normal UI. This would allow the majority of logic to be written once, while allowing each 
client to build its own UI with its own native toolkit.
