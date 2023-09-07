---
---

# {{ page.title }}

## Overview

Ballast has an official Intellij plugin which offers several useful tools for developing applications with Ballast:

- Real-time inspection of the status and data within all ViewModel features
- Time-travel debugging
- Templates for creating new Ballast components

The plugin is still in its early days of development, but will be gaining more features and additional configuration 
settings as time goes on. This page documents how to install and use all the features of the Intellij plugin, while the 
[Ballast Debugger][2] page shows how to install the debugger into your application so it can connect to the plugin.

## Usage

### Debugger

The following videos show some example usage of the debugger

<iframe width="560" height="315" src="https://www.youtube.com/embed/KBUIdMzYdCo?si=c800_AW72TALYC9e" title="Ballast Debugger Example" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" allowfullscreen></iframe>

#### Connecting to the debugger

Once installed, a new "Ballast Debugger" tool window will be added to the bottom-right of the IDE, which can be opened
to start the debugger. The debugger communicates via websockets to client applications that have the 
[Ballast Debugger][2] interceptor installed. The debugger communicates over localhost on port `9684`, which can be 
changed from within the Preferences dialog. For desktop and other applications not running in a virtual machine, you can 
connect using the normal loopback interface at `127.0.0.1`. Android emulators must use the emulated device's alias 
to the host computer's loopback at `10.0.2.2`.

The debugger's websocket server will only be active for as long as the tool window is open, but the client interceptors
will continually attempt to reconnect to the server if the connection is terminated (such as by closing the tool 
window). The clients attempt a reconnection every few seconds, and any time it needs to send an event to the server. If
the tool window is open, simply by interacting with your app it will reconnect to the debugger UI in the Intellij 
plugin, there is no need to restart your application or force a reconnection attempt.

Once connected, the client connection will send all events from its connected ViewModels through the websocket, and be 
interpreted by the server and displayed in the tool window in real-time. The connection will also send a heartbeat every
few seconds, so you can see whether the connection is still alive, even if nothing is happening in your ViewModels.

#### Using the Debugger

Once connected, the connection will be assigned a UUID and added to the "Connections" dropdown, with the most recent 
connections at the top of the list. You can click the button to the left of the connection dropdown to clear all data 
from the debugger. You should typically have 1 connection per app launch, but multiple devices may be connected to the
same debugger simultanously.

After selecting a connection, you can then select a ViewModel from the adjacent dropdown to browse the data in that
ViewModel.

When a ViewModel is selected, a series of tabs will be displayed in the UI, for browsing the different types of data 
reported by the debugger client. The tab icons will be hightlighted if that type of data has anything processing. For 
example, You can also choose via the plugin settings to always show the Current State, or if you're using 
[Ballast Navigation][6] to always show the current Route. 

You can select the tabs to show a list of data reported for that type, ordered by time. Some tabs (like interceptors) 
are only available for clients running a specific version of Ballast, since the necessary data for that tab is only 
supplied by clients using specifc versions of the Ballast Debugger Client.

By default, the data displayed when focusing a State, Input, or Event is the `.toString()` representation of the object.
You may customize the text display of these objects by overriding their `.toString()` values, or by providing an 
appropriate `DebuggerAdapter` (such as `JsonDebuggerAdapter` to serialize the values to JSON using 
`kotlinx.serialization`). 

For Inputs and States, you can copy their JSON representations to send back to the device, dynamically manipulating the
ViewModel remotely without the need for recompiling the app. This is handled automatically by providing an appropriate 
`DebuggerAdapter` which can deserialize JSON back into proper classes. See the [debugger client documentation][2] for 
more detail on how to set this up in your application.

### Scaffolding

Ballast inherently involves a fair amount of boilerplate for each screen, but much of this boilerplate can be 
automatically generated for you. The Intellij Plugin comes with a series of templates to generate this boilerplate, and
a handful of options to let you customize the templates to your needs.

You can quickly create files for new Ballast components from the file explorers "Right-click > New" menu, using 
Intellij's [File and Code Templates feature][5]. See the following clip for example usage in a Compose Desktop 
application.

<iframe width="560" height="315" src="https://www.youtube.com/embed/fDdF4E5u7SQ?si=_d0KJtfpHPQHdEs0" title="Ballast Intellij Plugin Templates" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" allowfullscreen></iframe>

You can also change the content generated from any template in `Preferences > Editor > File and Code Templates > Other`,
though this is not recommended as future changes to the templates in the Intellij plugin will not be reflected 
automatically in your edited version.

[1]: https://plugins.jetbrains.com/plugin/18439-compose-for-ide-plugin-development-experimental-
[2]: {{ 'Ballast Debugger' | link }}
[3]: {{ 'Ballast Repository' | link }}
[4]: {{ 'Ballast Saved State' | link }}
[5]: https://www.jetbrains.com/help/idea/settings-file-and-code-templates.html
[6]: {{ 'Ballast Navigation' | link }}

### Plugin Settings

Settings for the Ballast Intellij Plugin can be found the IDE settings at "Settings > Tools > Ballast".


## Installation

<div id="intellij-plugin-button"></div>
<br>

The button above will take you to the plugin landing page, or you can search for "Ballast" in the plugin marketplace
within IntelliJ-based IDEs. Note that the plugin's UI is built with [Compose for IDE Plugin Development][1], which is
still very early and only available in the latest versions of IntelliJ IDEA. It should work in both Community and
Ultimate editions on IntelliJ IDEA, however, at this time, the latest stable version of Android Studio is not supported.
