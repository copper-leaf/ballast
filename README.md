# Ballast

> Opinionated Application State Management framework for Kotlin Multiplatform

![Kotlin Version](https://img.shields.io/badge/Kotlin-2.2.20-orange)
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/copper-leaf/ballast)](https://github.com/copper-leaf/ballast/releases)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.copper-leaf/ballast-core)](https://search.maven.org/artifact/io.github.copper-leaf/ballast-core)
[![Intellij Plugin Version](https://img.shields.io/jetbrains/plugin/v/18702-ballast)](https://plugins.jetbrains.com/plugin/18702-ballast)

```kotlin
object TodosContract {
    data class State(
        val loading: Boolean = false,
        val todos: List<String> = emptyList(),
    )

    sealed interface Inputs {
        data object FetchSavedTodos : Inputs
        data class AddTodo(val text: String) : Inputs
        data class RemoveTodo(val text: String) : Inputs
    }
}

class TodosInputHandler : InputHandler<Inputs, Events, State> {
    override suspend fun InputHandlerScope<Inputs, Events, State>.handleInput(
        input: TodosContract.Inputs
    ) = when (input) {
        is FetchSavedTodos -> {
            updateState { copy(loading = true) }
            val todos = todosApi.fetchTodos()
            updateState { copy(loading = false, todos = todos) }
        }
        is AddTodo -> updateState { copy(todos = todos + input.text) }
        is RemoveTodo -> updateState { copy(todos = todos - input.text) }
    }
}

@Composable
fun App() {
    val coroutineScope = rememberCoroutineScope()
    val vm = remember(coroutineScope) { TodosViewModel(coroutineScope) }
    val vmState by vm.observeStates().collectAsState()

    LaunchedEffect(vm) { vm.send(TodosContract.FetchSavedTodos) }

    TodosList(vmState, postInput = { vm.trySend(it) })
}
```

_This snippet omits some details for brevity. See [Getting Started](docs/getting-started.md) for a complete walkthrough._

## Supported Platforms

Ballast was intentionally designed to not be tied to any particular platform or UI toolkit. It works in any Kotlin
target that supports Coroutines and Flows. The following platforms are officially supported and tested:

| Platform | Supported |
|----------|-----------|
| JVM      | ✅         |
| Android  | ✅         |
| iOS      | ✅         |
| JS       | ✅         |
| WASM JS  | ✅         |

## Installation

```kotlin
repositories {
    mavenCentral()
}

// for plain JVM or Android projects
dependencies {
    implementation("io.github.copper-leaf:ballast-core:{{ballastVersion}}")
    testImplementation("io.github.copper-leaf:ballast-test:{{ballastVersion}}")
}

// for multiplatform projects
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-core:{{ballastVersion}}")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-test:{{ballastVersion}}")
            }
        }
    }
}
```

Other modules can be added as needed. See [docs/README.md](docs/README.md) for the full list.

## Documentation

Full documentation is in the [docs/](docs/) directory:

- **[Getting Started](docs/getting-started.md)** — step-by-step guide to building your first Ballast screen
- **[Feature Overview](docs/feature-overview.md)** — core concepts: Contracts, Handlers, Side Jobs, Interceptors
- **[Thinking in Ballast MVI](docs/mental-model.md)** — the MVI model and Ballast's design philosophy
- **[Feature Comparison](docs/feature-comparison.md)** — Ballast vs Redux, Orbit, MVIKotlin, Uniflow-kt
- **[Migration Guides](docs/migration/)** — upgrading between major versions
- **[All modules and examples](docs/README.md)** — index of every module and example with descriptions

## Community

Join us on the Kotlin Slack in the [`#ballast`](https://kotlinlang.slack.com/archives/C03GTEJ9Y3E) channel for
support or to show off what you're building with Ballast.

## License

Ballast is licensed under the BSD 3-Clause License, see [LICENSE.md](LICENSE.md).

## References

Ballast was built upon years of experience building UI applications and observing the direction UI programming has
gone. The MVI model has proven itself robust across a wide range of applications, programming languages, and API 
surfaces. Ballast is not the only MVI library in Kotlin, but it is unique in being a highly opinionated and highly 
structured MVI library, which brings certain advantages. The [feature comparison](docs/feature-comparison.md) 
is a detailed breakdown of similarities and differences among the many libraries that were consulted as a large 
inspiration for Ballast. But by far, these 3 links were the most helpful in shaping how Ballast works and looks, and 
studying these resources may give you a deeper understanding of why Ballast was built the way that it was.

- [Redux](https://github.com/reduxjs/redux): The most widely-known implementation of the MVI/Flux pattern. React+Redux 
  has been one of the biggest contributors to this pattern's popularity.
- [Orbit MVI](https://github.com/orbit-mvi/orbit-mvi): A primary source of inspiration for Ballast. Mature and 
  well-built, though oriented closely toward Android and missing some features like a graphical debugger.
- [How to write your own MVI system and why you shouldn't](https://www.youtube.com/watch?v=E6obYmkkdko): An intro video 
  to the Orbit MVI library and one of the best introductions to the MVI model available. Walking through building a 
  simple MVI library from scratch helps cement the concepts behind using a mature one.
