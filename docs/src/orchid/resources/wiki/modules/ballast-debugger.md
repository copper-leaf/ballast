---
---

# {{ page.title }}

## Overview

Ballast Debugger is a tool for inspecting the status of all components in your Ballast ViewModels in graphical UI. It 
consists of a client library which you install into your Ballast ViewModels as an Interceptor, and a companion Intellij
plugin which displays the data collected from the interceptor and allows you to browse and manipulate the ViewModels 
remotely. The client library communicates with the UI over Websockets over localhost, so is intended to be used when
running your application in an simulator/emulator or in the browser.

It supports features one would expect from an MVI graphical debugger:

- Inspecting the status and data within all ViewModel features in real-time
- Time-travel debugging
- Direct State manipulation
- Remotely send Inputs
- Viewing ViewModel logs
- ([coming soon][5]) reporting custom metrics
- ([coming soon][6]) recording and replaying a series of Inputs

The Ballast Debugger must first be installed as a plugin in IntelliJ Idea (Community or Ultimate) then add the
[`ballast-debugger-client`](#Installation) dependency to your project and installed into your ViewModels as an 
Interceptor. This page documents how to set up the debugger library in your application, while the 
[Ballast Intellij Plugin][4] page demonstrates usage of the debugger UI, as well as the other features of the Intellij 
plugin.

## Basic Configuration

You will need to create a `BallastDebuggerClientConnection` with your choice of [Ktor client engine][1] and connect it 
on an application-wide CoroutineScope. This will start a Websocket connection to the IntelliJ plugin's server over 
localhost on port `9684` (by default, you can customize both the host and the port). The connection will automatically 
retry the connection until it succeeds, and reconnect if the connection is terminated. Finally, add the 
`BallastDebuggerInterceptor` which will send all its data through the websocket and be captured and displayed on the 
plugin's UI.

{% alert 'info' :: compileAs('md') %}
**Info**

The same connection should be shared among all ViewModels, to optimize the system resource usage and make it easier to
explore in the Debugger UI. All ViewModels on the same Connection will be grouped together in the UI.
{% endalert %}

{% alert 'danger' :: compileAs('md') %}
**Danger**

As the debugger will drain system resources and potentially leak sensitive information, you must **make sure** the 
debugger is not running in production. Configure your app to only start the connection and install the interceptor in 
debug builds, or better yet, only include the debugger dependency in debug builds, so you know it could never be running 
accidentally.
{% endalert %}

```kotlin
private val debuggerConnection by lazy {
    // or provide the scope from somewhere else
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    BallastDebuggerClientConnection(
        engineFactory = CIO,
        applicationCoroutineScope = applicationScope,
        host = "127.0.0.1", // 10.0.2.2 on Android
    ){
        // CIO Ktor client engine configuration
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
            if(DEBUG) { // some build-time constant
                this += BallastDebuggerInterceptor(debuggerConnection)
            }
        }
        .build(),
    eventHandler = ExampleEventHandler(),
)
```

## State/Input Serialization (v4+)

Since version 4.0.0, the Debugger allows you to send JSON (or other serialized content) from the graphical UI back to 
the connected ViewModel, where the content is deserialized and processed as if it were send from the application itself.
This allows you to directly manipulate the state or take UI actions without needing to hardcode it or recompile your 
application. The current UI for this feature is faily basic, but it will be improved in future releases without needing
any additional configuration in the client application.

Because Kotlin is a strongly-typed language, you must opt-in to this feature by enabling your State and Input classes to
be serializable, and letting the Interceptor know how to deserialize. You can use any serialization format/library you 
would like, the general process will be the same for everything.

You can configure the Interceptor to serialize States, Inputs, and Events so all of them will be displayed as JSON in 
the Debugger UI. Additionally, States and Inputs can be deserialized, so that the application can process JSON sent from
the debugger.

### kotlinx.serialization

The simplest way to enable this feature is to use the `kotlinx.serialization` library. The Debugger internally already 
uses this library for its own internal communication, so you only need to mark your State and Input classes as 
`@Serializable` and provide the serializers to the Interceptor. The compiler plugin will ensure all values in your 
State and Input classes are also serializable, and because the Ballast convention for Inputs is `sealed interface`, the 
Serialization lib automatically generates the contextual information for each Input subclass.

```kotlin
object ExampleContract {
    @Serializable
    data class State(
        val count: Int = 0
    )

    @Serializable
    sealed interface Inputs {
        @Serializable
        data class Increment(val amount: Int) : Inputs
        @Serializable
        data class Decrement(val amount: Int) : Inputs
    }

    @Serializable
    sealed interface Events {
    }
}
```

Once you've annotated your State and Input classes, you then provide the generated serialized to the 
`BallastDebuggerInterceptor`, or create a `JsonDebuggerAdapter` with the serializers and pass that to the Interceptor.

```kotlin
// example passing the serializers directly to the BallastDebuggerInterceptor
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
            if(DEBUG) {
                this += BallastDebuggerInterceptor(
                    debuggerConnection,
                    inputsSerializer = ExampleContract.Inputs.serializer(),
                    eventsSerializer = ExampleContract.Events.serializer(),
                    stateSerializer = ExampleContract.State.serializer(),
                )
            }
        }
        .build(),
    eventHandler = ExampleEventHandler(),
)
```

```kotlin
// example of using the JsonAdapter instead
val exampleContractAdapter = JsonDebuggerAdapter(
    inputsSerializer = ExampleContract.Inputs.serializer(),
    eventsSerializer = ExampleContract.Events.serializer(),
    stateSerializer = ExampleContract.State.serializer(),
    json = Json { },
)

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
            if(DEBUG) {
                this += BallastDebuggerInterceptor(
                    debuggerConnection,
                    adapter = exampleContractAdapter
                )
            }
        }
        .build(),
    eventHandler = ExampleEventHandler(),
)
```

### Alternative formats/libraries

If you would like to use a different format to (de)serialize your States and Inputs (such as XML), or would like to use 
another library (like Moshi or Jackson), the setup process will look mostly the same as when using 
`kotlinx.serialization`, except you'll need to provide your own `DebuggerAdapter` to handle the serialization needs. 

For example, here's what an adapter might look like when using Moshi (de)serialization. Other non-JSON formats would be
configured in exactly the same way, using the appropriate libraries and serialization logic for those other fo4rmats. 
This Moshi adapter requires the following Moshi dependencies:

```kotlin
// build.gradle.kts
kotlin {
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation("com.squareup.moshi:moshi:1.14.0")
                implementation("com.squareup.moshi:moshi-kotlin:1.14.0")
                implementation("com.squareup.moshi:moshi-adapters:1.8.0")
            }
        }
    }
}
```

```kotlin
class MoshiReflectionDebuggerAdapter<Inputs : Any, Events : Any, State : Any>(
    private val inputsJsonAdapter: JsonAdapter<Inputs>,
    private val eventsJsonAdapter: JsonAdapter<Events>,
    private val stateJsonAdapter: JsonAdapter<State>,
) : DebuggerAdapter<Inputs, Events, State> {
    override fun serializeInput(input: Inputs): Pair<ContentType, String> {
        return ContentType.Application.Json to inputsJsonAdapter.toJson(input)
    }

    override fun serializeEvent(event: Events): Pair<ContentType, String> {
        return ContentType.Application.Json to eventsJsonAdapter.toJson(event)
    }

    override fun serializeState(state: State): Pair<ContentType, String> {
        return ContentType.Application.Json to stateJsonAdapter.toJson(state)
    }

    override fun deserializeInput(contentType: ContentType, serializedInput: String): Inputs? {
        check(contentType == ContentType.Application.Json)
        return inputsJsonAdapter.fromJson(serializedInput)
    }

    override fun deserializeState(contentType: ContentType, serializedState: String): State? {
        check(contentType == ContentType.Application.Json)
        return stateJsonAdapter.fromJson(serializedState)
    }

    override fun toString(): String {
        return "MoshiReflectionDebuggerAdapter"
    }

    companion object {
        @ExperimentalStdlibApi
        inline operator fun <reified Inputs : Any, reified Events : Any, reified State : Any> invoke(
        ): MoshiReflectionDebuggerAdapter<Inputs, Events, State> {
            val inputsPolymorphicFactory = Inputs::class
                .sealedSubclasses
                .fold(
                    initial = PolymorphicJsonAdapterFactory.of(Inputs::class.java, "inputClass")
                ) { acc, next -> acc.withSubtype(next.java, next.java.name) }
            val eventsPolymorphicFactory = Events::class
                .sealedSubclasses
                .fold(
                    initial = PolymorphicJsonAdapterFactory.of(Events::class.java, "eventClass")
                ) { acc, next -> acc.withSubtype(next.java, next.java.name) }

            val moshi: Moshi = Moshi
                .Builder()
                .add(inputsPolymorphicFactory)
                .add(eventsPolymorphicFactory)
                .addLast(KotlinJsonAdapterFactory())
                .build()

            return MoshiReflectionDebuggerAdapter(
                inputsJsonAdapter = moshi.adapter<Inputs>(),
                eventsJsonAdapter = moshi.adapter<Events>(),
                stateJsonAdapter = moshi.adapter<State>(),
            )
        }
    }
}
```

```kotlin
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
            if(DEBUG) {
                this += BallastDebuggerInterceptor(
                    debuggerConnection,
                    adapter = MoshiReflectionDebuggerAdapter()
                )
            }
        }
        .build(),
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
    implementation("io.github.copper-leaf:ballast-debugger-client:{{site.version}}")
}

// for multiplatform projects
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-debugger-client:{{site.version}}")
            }
        }
    }
}
```

### Android

On Android you need to add a clear text traffic permission for `10.0.2.2` to your network security configuration.

To do that you need to create the file `network_security_config.xml` at `src/main/res/xml` in your Android module. The content should look like this:

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config xmlns:android="http://schemas.android.com/apk/res/android">
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="false">10.0.2.2</domain>
    </domain-config>
</network-security-config>
```

Then, in your `AndroidManifest.xml` add the following line to your `application` configuration:

```xml
<application
  ...
  android:networkSecurityConfig="@xml/network_security_config" >
    ...
</application>
```

[1]: https://ktor.io/docs/http-client-engines.html
[2]: https://plugins.jetbrains.com/plugin/18702-ballast/versions
[3]: https://www.jetbrains.com/help/idea/managing-plugins.html#install_plugin_from_disk
[4]: {{ 'Ballast Intellij Plugin' | link }}
[5]: https://github.com/copper-leaf/ballast/issues/48
[6]: https://github.com/copper-leaf/ballast/issues/51
