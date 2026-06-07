# Ballast Debugger Client

## Overview

Ballast Debugger is a tool for inspecting the status of all components in your Ballast ViewModels through a graphical
UI. It consists of a client library which you install into your Ballast ViewModels as an Interceptor, and a companion
[IntelliJ plugin](./../ballast-idea-plugin) which displays the data collected from the interceptor and allows you to
browse and manipulate the ViewModels remotely. The client library communicates with the UI over WebSockets on
localhost, so it is intended to be used when running your application in a simulator/emulator or in the browser.

Features:

- Inspecting the status and data within all ViewModel features in real-time
- Time-travel debugging
- Direct State manipulation
- Remotely send Inputs
- Viewing ViewModel logs

## Supported Platforms

| Platform | Supported |
|----------|-----------|
| JVM      | ✅         |
| Android  | ✅         |
| iOS      | ✅         |
| JS       | ✅         |
| WASM JS  | ✅         |

## See Also

- [Ballast Kotlinx Serialization](./../ballast-kotlinx-serialization)
- [Ballast IntelliJ Plugin](./../ballast-idea-plugin)

## Usage

### Basic Configuration

Create a `BallastDebuggerClientConnection` with your choice of Ktor client engine and connect it on an
application-wide `CoroutineScope`. This starts a WebSocket connection to the IntelliJ plugin's server on localhost
port `9684` (the host and port are both configurable). The connection will automatically retry until it succeeds and
reconnect if terminated.

The same connection should be shared among all ViewModels to optimize system resource usage and to group all
ViewModels together in the debugger UI.

> **Warning:** The debugger drains system resources and potentially exposes sensitive information. You must ensure the
> debugger is not running in production. Configure your app to only start the connection and install the interceptor in
> debug builds — or better yet, only include the debugger dependency in debug builds so it can never run accidentally.

```kotlin
private val debuggerConnection by lazy {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    BallastDebuggerClientConnection(
        engineFactory = CIO,
        applicationCoroutineScope = applicationScope,
        host = "127.0.0.1", // use 10.0.2.2 when connecting from an Android emulator
    ) {
        // optional Ktor client engine configuration
    }.also { it.connect() }
}

class ExampleViewModel(coroutineScope: CoroutineScope) : BasicViewModel<
        ExampleContract.Inputs,
        ExampleContract.Events,
        ExampleContract.State>(
    coroutineScope = coroutineScope,
    config = BallastViewModelConfiguration.Builder()
        .withViewModel(
            initialState = ExampleContract.State(),
            inputHandler = ExampleInputHandler(),
            name = "Example",
        )
        .apply {
            if (DEBUG) {
                this += BallastDebuggerInterceptor(debuggerConnection)
            }
        }
        .build(),
    eventHandler = ExampleEventHandler(),
)
```

### Android

On Android, connecting to the emulator's host machine requires cleartext traffic to `10.0.2.2`. Add a network security
configuration to permit this.

Create `src/main/res/xml/network_security_config.xml` in your Android module:

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config xmlns:android="http://schemas.android.com/apk/res/android">
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="false">10.0.2.2</domain>
    </domain-config>
</network-security-config>
```

Then reference it in your `AndroidManifest.xml`:

```xml
<application
    ...
    android:networkSecurityConfig="@xml/network_security_config">
    ...
</application>
```

### State/Input Serialization

Since v4.0.0, the Debugger allows you to send JSON from the graphical UI back to the connected ViewModel, where the
content is deserialized and processed as if sent from the application itself. This enables direct State manipulation and
sending Inputs remotely without recompiling your app.

To opt in, make your State, Input, and Event classes serializable and tell the Interceptor how to deserialize them.

#### kotlinx.serialization

The simplest approach. Mark your classes with `@Serializable` and provide the generated serializers to the Interceptor:

```kotlin
object ExampleContract {
    @Serializable
    data class State(val count: Int = 0)

    @Serializable
    sealed interface Inputs {
        @Serializable
        data class Increment(val amount: Int) : Inputs
        @Serializable
        data class Decrement(val amount: Int) : Inputs
    }

    @Serializable
    sealed interface Events
}
```

Pass the serializers directly to `BallastDebuggerInterceptor`:

```kotlin
this += BallastDebuggerInterceptor(
    debuggerConnection,
    inputsSerializer = ExampleContract.Inputs.serializer(),
    eventsSerializer = ExampleContract.Events.serializer(),
    stateSerializer = ExampleContract.State.serializer(),
)
```

Or wrap them in a `JsonDebuggerAdapter`:

```kotlin
val adapter = JsonDebuggerAdapter(
    inputsSerializer = ExampleContract.Inputs.serializer(),
    eventsSerializer = ExampleContract.Events.serializer(),
    stateSerializer = ExampleContract.State.serializer(),
    json = Json { },
)

this += BallastDebuggerInterceptor(debuggerConnection, adapter = adapter)
```

#### Alternative serialization formats

To use a different library (e.g. Moshi, Jackson) or format (e.g. XML), implement your own `DebuggerAdapter`:

```kotlin
class MoshiDebuggerAdapter<Inputs : Any, Events : Any, State : Any>(
    private val inputsAdapter: JsonAdapter<Inputs>,
    private val eventsAdapter: JsonAdapter<Events>,
    private val stateAdapter: JsonAdapter<State>,
) : DebuggerAdapter<Inputs, Events, State> {
    override fun serializeInput(input: Inputs): Pair<ContentType, String> =
        ContentType.Application.Json to inputsAdapter.toJson(input)

    override fun serializeEvent(event: Events): Pair<ContentType, String> =
        ContentType.Application.Json to eventsAdapter.toJson(event)

    override fun serializeState(state: State): Pair<ContentType, String> =
        ContentType.Application.Json to stateAdapter.toJson(state)

    override fun deserializeInput(contentType: ContentType, serializedInput: String): Inputs? {
        check(contentType == ContentType.Application.Json)
        return inputsAdapter.fromJson(serializedInput)
    }

    override fun deserializeState(contentType: ContentType, serializedState: String): State? {
        check(contentType == ContentType.Application.Json)
        return stateAdapter.fromJson(serializedState)
    }
}
```

Then pass an instance to the Interceptor:

```kotlin
this += BallastDebuggerInterceptor(debuggerConnection, adapter = MoshiDebuggerAdapter(...))
```

## Installation

```kotlin
repositories {
    mavenCentral()
}

// for plain JVM or Android projects
dependencies {
    debugImplementation("io.github.copper-leaf:ballast-debugger-client:{{ballastVersion}}")
}

// for multiplatform projects
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-debugger-client:{{ballastVersion}}")
            }
        }
    }
}
```
