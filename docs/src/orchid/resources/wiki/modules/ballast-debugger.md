---
---

# {{ page.title }}

## Overview

Ballast Debugger is a tool for inspecting the status of all components in your Ballast ViewModels in a IntelliJ plugin.
It supports features one would expect from an MVI graphical debugger:

- Inspecting the status and data within all ViewModel features in real-time
- Time-travel debugging
- Resending Inputs for re-processing

The Ballast Debugger must first be installed as a plugin in IntelliJ Idea (Community or Ultimate) then add the
[`ballast-debugger`](#Installation) dependency to your project and installed into your ViewModels as an Interceptor. 
This page documents how to set up the debugger library in your application, while the 
[Ballast Intellij Plugin][4] page demonstrates usage of the debugger UI, as well as the other features of the Intellij 
plugin.

## Usage

You will need to create a `BallastDebuggerClientConnection` with your choice of [Ktor client engine][1] and connect it 
on an application-wide CoroutineScope. This will start a Websocket connection to the IntelliJ plugin's server over 
localhost on port `9684` (by default, this can be changed). The connection will automatically retry the connection until 
it succeeds, and reconnect if the connection is terminated. Finally, add the`BallastDebuggerInterceptor` which will send 
all its data through the websocket and be captured and displayed on the plugin's UI.

{% alert 'info' :: compileAs('md') %}
The same connection should be shared among all ViewModels, to optimize the system resource usage and make it easier to
explore in the Debugger UI.
{% endalert %}

{% alert 'danger' :: compileAs('md') %}
As the debugger will drain system resources and potentially leak sensitive information, you must **make sure** the 
debugger is not running in production. Configure your app to only start the connection and install the interceptor in 
debug builds, or better yet, only include the debugger dependency in debug builds, so you know it could never be running 
accidentally.
{% endalert %}

```kotlin
val debuggerConnection by lazy {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    BallastDebuggerClientConnection(
        CIO,
        applicationScope, 
        host = "127.0.0.1", // 10.0.2.2 on Android
    ) { 
        // CIO Ktor client engine configuration
    }
    .also { it.connect() }
}

class ExampleViewModel(coroutineScope: CoroutineScope) : BasicViewModel<
        ExampleContract.Inputs,
        ExampleContract.Events,
        ExampleContract.State>(
    coroutineScope = coroutineScope, 
    config = BallastViewModelConfiguration.Builder()
        .apply {
            if(DEBUG) { // some build-time constant
                this += BallastDebuggerInterceptor(debuggerConnection)
            }
        }
        .forViewModel(
            initialState = ExampleContract.State(),
            inputHandler = ExampleInputHandler(),
            name = "Example",
        ),
    eventHandler = ExampleEventHandler(),
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

[1]: https://ktor.io/docs/http-client-engines.html
[2]: https://plugins.jetbrains.com/plugin/18702-ballast/versions
[3]: https://www.jetbrains.com/help/idea/managing-plugins.html#install_plugin_from_disk
[4]: {{ 'Ballast Intellij Plugin' | link }}
