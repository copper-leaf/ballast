---
---

# Ballast

> Opinionated Application State Management framework for Kotlin Multiplatform

![Kotlin Version](https://img.shields.io/badge/Kotlin-1.6.10-orange)
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/copper-leaf/ballast)](https://github.com/copper-leaf/ballast/releases)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.copper-leaf/ballast-core)](https://search.maven.org/artifact/io.github.copper-leaf/ballast-core)
[![Intellij Plugin Version](https://img.shields.io/jetbrains/plugin/v/18702-ballast)](https://plugins.jetbrains.com/plugin/18702-ballast)

```kotlin
object TodosContract { 
    data class State(
        val loading: Boolean = false, 
        val todos: List<String> = emptyList(), 
    )
  
    sealed class Inputs {
        object FetchSavedTodos : Inputs()
        data class AddTodo(val text: String) : Inputs()
        data class RemoveTodo(val text: String) : Inputs()
    }
}

class TodosInputHandler : InputHandler<Inputs, Events, State> {
    override suspend fun InputHandlerScope<Inputs, Events, State>.handleInput(
        input: TodosContract.Inputs
    ) = when (input) {
        is FetchSavedTodos -> { 
            updateState { it.copy(loading = true) }
            val todos = todosApi.fetchTodos()
            updateState { it.copy(loading = false, todos = todos) }
        }
        is AddTodo -> {
            updateState { it.copy(todos = it.todos + input.text) }
        }
        is RemoveTodo -> {
            updateState { it.copy(todos = it.todos - input.text) }
        }
    }
}

@Composable
fun App() { 
    val coroutineScope = rememberCoroutineScope()
    val vm = remember(coroutineScope) { TodosViewModel(coroutineScope) }
    val vmState by vm.observeStates().collectAsState()
    
    LaunchedEffect(vm) { 
        vm.send(TodosContract.FetchSavedTodos)
    }
    
    TodosList(vmState) { vm.trySend(it) }
}

@Composable
fun TodosList(
    vmState: TodosContract.State,
    postInput: (TodosContract.Inputs)->Unit,
) {
    // ...
}
```

* _This snippet omits some details for brevity, to demonstrate the general idea_

# Supported Platforms/Features

Ballast was intentionally designed to not be tied directly to any particular platform or UI toolkit. In fact, while most
Kotlin MVI libraries were initially developed for Android and show many artifacts of that initial base, Ballast started
as a State Management solution for Compose Desktop.

Because Ballast was initially designed entirely in a non-Android context, it should work in any Kotlin target or
platform as long as it works with Coroutines and Flows. However, the following targets are officially supported, in
that they have been tested and are known to work there, or have specific features for that platform

- [Android](https://copper-leaf.github.io/ballast/wiki/platforms/android)
- [iOS](https://copper-leaf.github.io/ballast/wiki/platforms/ios) (requires new Kotlin Native Memory Model)
- [Compose Desktop](https://copper-leaf.github.io/ballast/wiki/platforms/compose-desktop)
- [Compose Web](https://copper-leaf.github.io/ballast/wiki/platforms/compose-web)

# Installation

```kotlin
repositories {
    mavenCentral()
}

// for plain JVM or Android projects
dependencies {
    implementation("io.github.copper-leaf:ballast-core:{{site.version}}")
    implementation("io.github.copper-leaf:ballast-repository:{{site.version}}")
    implementation("io.github.copper-leaf:ballast-debugger:{{site.version}}")
    implementation("io.github.copper-leaf:ballast-firebase-crashlytics:{{site.version}}")
    implementation("io.github.copper-leaf:ballast-firebase-analytics:{{site.version}}")
    testImplementation("io.github.copper-leaf:ballast-test:{{site.version}}")
}

// for multiplatform projects
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
              implementation("io.github.copper-leaf:ballast-core:{{site.version}}")
              implementation("io.github.copper-leaf:ballast-repository:{{site.version}}")
              implementation("io.github.copper-leaf:ballast-debugger:{{site.version}}")
              implementation("io.github.copper-leaf:ballast-firebase-crashlytics:{{site.version}}")
              implementation("io.github.copper-leaf:ballast-firebase-analytics:{{site.version}}")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-test:{{site.version}}")
            }
        }
    }
}
```

# Documentation

See the [website](https://copper-leaf.github.io/ballast/) for detailed documentation and usage instructions.

# License

Ballast is licensed under the BSD 3-Clause License, see [LICENSE.md](https://github.com/copper-leaf/ballast/tree/main/LICENSE.md).

# References

Ballast is not new, it was built upon years of experience building UI applications in Android and observing the
direction UI programming has gone in the past few years. The MVI model has proven itself to be robust to a wide array
of applications, and there are different implementations of the pattern that focus on different aspects of the pattern.

The following are some of the main libraries I drew inspiration from while using Ballast. If Ballast does not fit your
project's needs, maybe one of these will suit you better. See the [feature comparison][4] for a better breakdown of the
specific features of these libraries, to demonstrate the similarities and differences between them.

- [Redux][1]: The OG of the MVI programming model. It also was not the first MVI library, but React+Redux has certainly
  been one of the biggest contributors to this pattern's popularity today, especially in JS, but also in many other
  tech spaces
- [Orbit MVI][2]: A primary source of inspiration for Ballast. This library is mature and well-built, but in my opinion
  was built a little too closely to Android, making it less useful on other KMP targets. It also uses terminology from
  Redux like "reducer" and "transformer" that are intended to bridge the gap from users familiar with Redux, but are
  a bit confusing for developers new to MVI. It is also missing some key features that one would expect from an MVI
  library, like a graphical debugger.
- [How to write your own MVI system and why you shouldn't][3]: An intro video to the [Orbit MVI][2] library, and one of
  the best introductions to the MVI model I've seen. By walking you through the thought process behind developing a
  simple MVI library, it reinforces the concepts of the pattern and helps you understand how to use a mature MVI library
  like Orbit or Ballast.


[1]: https://github.com/reduxjs/redux
[2]: https://github.com/orbit-mvi/orbit-mvi
[3]: https://www.youtube.com/watch?v=E6obYmkkdko
[4]: https://copper-leaf.github.io/ballast/wiki/feature-comparison/
