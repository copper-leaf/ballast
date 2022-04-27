---
---

# {{ page.title }}

Ballast Debugger is a tool for inspecting the status of all components in your Ballast ViewModels in a IntelliJ plugin.
It supports features one would expect from an MVI graphical debugger:

- Inspecting the status and data within all ViewModel features in real-time
- Time-travel debugging
- Resending Inputs for re-processing

The Ballast Debugger must first be installed as a plugin in IntelliJ Idea (Community or Ultimate) or Android Studio, and
then add the [`ballast-debugger`](#Installation) dependency to your project. This page documents how to set up the 
debugger library in your application, while the {{ 'Ballast Intellij Plugin' | anchor }} page demonstrates usage of the
debugger UI, as well as the other features of the Intellij plugin.

# Usage

You will need to create a `BallastDebuggerClientConnection` with your choice of [Ktor client engine][1] and connect it 
on an application-wide CoroutineScope. This will start a Websocket connection to the IntelliJ plugin's server over 
localhost on port 9684. The connection will automatically retry the connection until it succeeds. Finally, add the 
`BallastDebuggerInterceptor` which will send all its data through the websocket and be captured and displayed on the 
plugin's UI.

The same connection should be shared among all ViewModels. Also, you definitely do not want to start the connection or
include this interceptor in production builds, so make sure you configure your app to only use it in debug builds (or 
better yet, only include the debugger dependency in debug builds). 

Since Ballast is still an early project, the Debugger currently requires that the client connection library be the same
version as your IntelliJ plugin to avoid issues with differences in serialization between the two. If your IntelliJ
plugin version gets updated ahead of you project's library version, you can download the specific version you need from
the [marketplace page][2] and install it [from disk][3]. Alternatively, you may try hard-coding the IntelliJ plugin's
version to the `BallastDebuggerClientConnection` when created as a workaround, though I make no guarantees of 
compatibility and will not be making any fixes for backward compatibility. I expect this restriction to remain during
all 1.X.X versions to make sure the public API and serialization state is stable, but in 2.X.X and beyond will make sure
to maintain compatability between versions.

```kotlin
val debuggerConnection by lazy {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    BallastDebuggerClientConnection(
        OkHttp,
        applicationScope, 
        host = "127.0.0.1", // 10.1.1.20 on Android
    ) { 
        // OkHttp Ktor client engine configuration
    }
    .also { it.connect() }
}

@HiltViewModel
class ExampleViewModel
@Inject
constructor() : AndroidViewModel<
        ExampleContract.Inputs,
        ExampleContract.Events,
        ExampleContract.State>(
    config = BallastViewModelConfiguration.Builder()
        .apply {
            if(BuildConfig.DEBUG) {
                this += BallastDebuggerInterceptor(debuggerConnection)
            }
        }
        .forViewModel(
            initialState = ExampleContract.State(),
            inputHandler = ExampleInputHandler(),
            name = "Example",
        )
)
```

# Installation

```kotlin
repositories {
    mavenCentral()
}

// for plain JVM or Android projects
dependencies {
    implementation("io.github.copper-leaf:ballast-debugger:{{site.version}}")
}

// for multiplatform projects
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-debugger:{{site.version}}")
            }
        }
    }
}
```

[1]: https://ktor.io/docs/http-client-engines.html
[2]: https://plugins.jetbrains.com/plugin/18702-ballast/versions
[3]: https://www.jetbrains.com/help/idea/managing-plugins.html#install_plugin_from_disk
