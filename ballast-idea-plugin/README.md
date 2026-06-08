# Ballast IntelliJ Plugin

## Overview

Ballast has an official IntelliJ plugin which offers several useful tools for developing applications with Ballast:

- Real-time inspection of the status and data within all ViewModel features
- Time-travel debugging and direct State manipulation
- Code scaffolding templates for creating new Ballast components

The plugin is available in both Community and Ultimate editions of IntelliJ IDEA. Note that the plugin's UI is built
with Compose for IDE Plugin Development, which currently requires a recent version of IntelliJ IDEA — the latest
stable release of Android Studio is not supported at this time.

## Installation

Search for "Ballast" in the IntelliJ plugin marketplace (`Settings > Plugins > Marketplace`), or visit the
[plugin page on the JetBrains Marketplace](https://plugins.jetbrains.com/plugin/18702-ballast).

## Usage

### Debugger

The plugin works in conjunction with the [Ballast Debugger Client](./../ballast-debugger-client) library, which you
install into your application as an Interceptor. See that module's README for how to configure your app to connect.

Video walkthrough: https://www.youtube.com/watch?v=KBUIdMzYdCo

#### Connecting

Once installed, a "Ballast Debugger" tool window appears in the IDE. Open it to start the debugger server. The
debugger communicates over WebSockets on localhost port `9684` (configurable in `Settings > Tools > Ballast`).

- Desktop/JVM apps: connect using `127.0.0.1`
- Android emulators: connect using `10.0.2.2` (the emulated device's alias to the host loopback)

The debugger server is only active while the tool window is open. Client interceptors will continuously attempt to
reconnect if the connection is lost — simply reopening the tool window and interacting with your app is enough to
re-establish the connection without restarting the application.

#### Browsing ViewModel Data

Once connected, each app launch is assigned a UUID and added to the "Connections" dropdown (most recent at the top).
You can connect multiple devices simultaneously. Select a connection, then select a ViewModel from the adjacent
dropdown to browse its data.

When a ViewModel is selected, a series of tabs display the different types of data reported by the client: State,
Inputs, Events, Side Jobs, and Interceptors. Tab icons highlight when that type has anything actively processing.

The data for each item is displayed as its `.toString()` representation by default. You can customize this by
overriding `.toString()`, or by providing a `JsonDebuggerAdapter` to serialize values to JSON via
`kotlinx.serialization`.

#### Time-travel and Remote Manipulation

For Inputs and States that have serialization configured, you can copy their JSON representations and send them back
to the device — manipulating the ViewModel's State or triggering Inputs remotely without recompiling. See the
[Ballast Debugger Client](./../ballast-debugger-client) README for details on configuring serialization.

### Scaffolding Templates

Ballast inherently involves a fair amount of boilerplate for each screen, but the plugin can generate it for you.
Templates are available from the file explorer's "Right-click > New" menu using IntelliJ's File and Code Templates
feature.

Video walkthrough: https://www.youtube.com/watch?v=fDdF4E5u7SQ

You can customize the generated content in `Preferences > Editor > File and Code Templates > Other`, though note that
future plugin updates will not automatically update your edited versions.

### Plugin Settings

Settings for the Ballast IntelliJ Plugin can be found at `Settings > Tools > Ballast`.
