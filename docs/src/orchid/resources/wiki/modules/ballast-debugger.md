---
---

# Ballast Debugger

Ballast Debugger is a tool for inspecting the status of all components in your Ballast ViewModels in a IntelliJ plugin.
It supports features one would expect from an MVI graphical debugger:

<div id="intellij-plugin-button"></div>

- Inspecting the status and data within all ViewModel features in real-time
- Time-travel debugging
- Resending Inputs for re-processing
- Templates for creating new Ballast components (coming soon)
- Sample panel for getting a feel for the features of Ballast, which connects itself to the Debugger

The Ballast Debugger must first be installed as a plugin in IntelliJ Idea (Community or Ultimate) or Android Studio, and
then add the [`ballast-debugger`](#Installation) dependency to your project.

<div id="intellij-plugin-card"></div>

You will need to create a `BallastDebuggerClientConnection` with your choice of [Ktor client engine][1] and connect it 
on an application-wide CoroutineScope. This will start a Websocket connection to the IntelliJ plugin's server over 
localhost on port 9684. The connection will automatically retry the connection until it succeeds. Finally, add the 
`BallastDebuggerInterceptor` which will send all its data through the websocket and be captured and displayed on the 
plugin's UI.

The same connection should be shared among all ViewModels. Also, you definitely do not want to start the connection or
include this interceptor in production builds, so make sure you configure your app to only use it in debug builds (or 
better yet, only include the debugger dependency in debug builds).

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

## Installation

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

<script src="https://plugins.jetbrains.com/assets/scripts/mp-widget.js"></script>
<script>
  MarketplaceWidget.setupMarketplaceWidget('install', 18702, "#intellij-plugin-button");
  MarketplaceWidget.setupMarketplaceWidget('card', 18702, "#intellij-plugin-card");
</script>

[1]: https://ktor.io/docs/http-client-engines.html
