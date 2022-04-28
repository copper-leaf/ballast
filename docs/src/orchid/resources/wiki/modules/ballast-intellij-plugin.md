---
---

# {{ page.title }}

Ballast has an official Intellij plugin which offers several useful tools for developing applications with Ballast:

- Real-time inspection of the status and data within all ViewModel features
- Time-travel debugging
- Templates for creating new Ballast components

The plugin is still in its early days of development, but will be gaining more features and additional configuration 
settings as time goes on. This page documents how to install and use all the features of the Intellij plugin, while the 
[Ballast Debugger][2] page shows how to install the debugger into your application so it can connect to the plugin.

# Usage

## Debugger

### Installation

<div id="intellij-plugin-button"></div>
<br>

The button above will take you to the plugin landing page, or you can search for "Ballast" in the plugin marketplace
within IntelliJ-based IDEs. Note that the plugin's UI is built with [Compose for IDE Plugin Development][1], which is
still very early and only available in the latest versions of IntelliJ IDEA. It should work in both Community and
Ultimate editions on IntelliJ IDEA, however, at this time, the latest stable version of Android Studio is not supported.

### Connecting to the debugger

Once installed, a new "Ballast Debugger" tool window will be added to the bottom-right of the IDE, which can be opened
to start the debugger. The debugger communicates via websockets to client applications that have the 
[Ballast Debugger][2] interceptor installed. The debugger communicates over localhost on port `9684`, which can be 
changed from within the Preferences dialog. For desktop and other applications not running in a virtual machine, you can 
connect using the normal loopback interface at `127.0.0.1`. Android emulators must use the emulated device's alias 
to the host computer's loopback at `10.1.1.20`.

The debugger's websocket server will only be active for as long as the tool window is open, but the client interceptors
will continually attempt to reconnect to the server if the connection is terminated (such as by closing the tool 
window). The clients attempt a reconnection every few seconds, and any time it needs to send an event to the server. If
the tool window is open, simply by interacting with your app it will reconnect to the debugger UI in the Intellij 
plugin, there is no need to restart your application or force a reconnection attempt.

Once connected, the client connection will send all events from its connected ViewModels through the websocket, and be 
interpreted by the server and displayed in real-time. The connection will also send a heartbeat every few seconds, so
you can see whether the connection is still alive, even if nothing is happening in your ViewModels.

### Using the Debugger

Once connected, the connection will be assigned a UUID and added to the "Connections" column, with the most recent 
connections at the top of the list. You can right-click the header to clear all connections from that column.

By clicking a connection, it will open up a second column with all the ViewModels communicating through that connection.
You should typically have 1 connection per app launch, but multiple devices may be connected simultanously. You can 
right-click the connection to clear all the data from that connection, which is the same as right-clicking the header
of the second column.

Each ViewModel in the "ViewModels" column will highlight its icons when anything is being processed, and if more than
1 side job or Input are processing concurrently, a badge will be shown with the number of items being processed. If the
connection was established after the ViewModel started running, you can right-click the ViewModel and select "Refresh"
to request the client resend all the activity that may have been missed. Depending on how long the ViewModel has been 
running and how many Inputs were processed, this may take a few seconds to complete.

Clicking a ViewModel will focus it and show a tabbed view of States, Inputs, Events, and SideJobs. Each of these shows
a list of each discrete instance that has been processed, and the status of that item. Newest items are always at the 
top of the list. You can select any item to focus it and view more details about that item. Note that the text that 
shows is typically the result of calling `.toString()` on the object in the client, and so the actual representation
may vary between different platforms. 

## Scaffolding

You can quickly create files for new Ballast components from the file explorers "Right-click > New" menu, using 
Intellij's [File and Code Templates feature][5].

There are 3 options for creating new components, which themselves have several options for the components available to 
generate:

- **Ballast UI Component**: Components used for normal UI ViewModels (excluding the ViewModel class itself)
  - _Contract_: A simple Contract with `Initialize` and `GoBack` inputs which would be used by most screens
  - _InputHandler_: An InputHandler to process the sample Contract's Inputs
  - _EventHandler_: An empty EventHandler to process the sample Contract's Events
  - _SavedStateAdapter_: An adapter for the [SavedStateAdapter][4] module
- **Ballast ViewModel**: The ViewModel class for normal UI ViewModels
  - _BasicViewModel_: An implementation which can be used on any arbitrary platform, including Kotlin targets that don't have their own platform-specific ViewModel
  - _AndroidViewModel_: The implementation typically used on Android targets, which extends `androidx.lifecycle.ViewModel`
  - _IosViewModel_: The implementation required for iOS targets, which integrates with the Swift Combine framework
- **Ballast Repository Component**: The components and "ViewModel" class for [Ballast Repositories][3]
  - _Contract_: A simple Contract with `Cached` properties used by a `BallastRepository`
  - _Repository_: The simple Repository interface that is exposed to UI ViewModels
  - _InputHandler_: An InputHandler to process the sample Contract's Inputs
  - _RepositoryImpl_: A Repository implementation which can be used on any arbitrary platform, which exposes the sample Repository interface 
  - _AndroidRepositoryImpl_: A Repository implementation which extends `androidx.lifecycle.ViewModel`, which exposes the sample Repository interface

You can always generate just a single file at a time with the above-named templates, but there are some additional 
options which may generate more than one template in a single action, for your convenience.

You can also change the content generated from any template in `Preferences > Editor > File and Code Templates > Other`,
though this is not recommended as future changes to the templates in the Intellij plugin will not be reflected 
automatically in your edited version.

[1]: https://plugins.jetbrains.com/plugin/18439-compose-for-ide-plugin-development-experimental-
[2]: {{ 'Ballast Debugger' | link }}
[3]: {{ 'Ballast Repository' | link }}
[4]: {{ 'Ballast Saved State' | link }}
[5]: https://www.jetbrains.com/help/idea/settings-file-and-code-templates.html
