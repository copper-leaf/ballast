---
---

## Feature Summary

This page is a comparison of several MVI libraries, to help you understand how each library is similar or different from
the others. I sincerely believe Ballast is the best option for MVI state management in Kotlin, but that doesn't mean the
other libraries aren't good options too. Some of them might have a API that just clicks with you better, and that's 
perfectly fine. This comparison can help you figure out if Ballast is the right option for you, and if not, help you
determine your suitable alternative.

The obvious disclaimer is that this list is put together by the person behind Ballast, so I'm obviously a bit biased 
toward my own library. But I really do want this to be as objective of a comparison as possible, so if you see any 
errors or anything seems misleading, please let me know or submit a pull request to correct it! 

And to further combat bias, I'd recommend also checking out [this article][01] for a more in-depth comparison of 
these Android/Kotlin MVI libraries, which doesn't include Ballast. This article is from one of the developers of Orbit MVI.

The following libraries are compared in this article:

- Ballast
- [Redux][20]
- [Orbit][30]
- [MVIKotlin][40]
- [Uniflow-kt][50]

**Legend**

- <i class="fas fa-check-double" style="color: green"></i>&nbsp;Fully Officially supported&nbsp;&nbsp;
- <i class="fas fa-check" style="color: purple"></i>&nbsp;Fully supported by 3rd-party&nbsp;&nbsp;
- <i class="fas fa-minus-circle" style="color: #ebc634"></i>&nbsp;Partially supported&nbsp;&nbsp;
- <i class="fas fa-ban" style="color: red"></i>&nbsp;Not supported&nbsp;&nbsp;

## General

### General Philosophy

{% alert 'info' :: compileAs('md') %}
This refers to the general development philosophy behind the development of the library, such as whether it's aiming
to be lightweight or fully featured, as well as any other significant notes about how to approach the library.
{% endalert %}

- **Ballast**: Opinionated Application State Management framework for all KMP targets
- **Redux**: Lightweight JS UI State Management library, with many official and unofficial extensions
- **Orbit**: Fully-featured, low-profile UI MVI framework for Android
- **MVIKotlin**: Redux implementation in Kotlin for Android
- **Uniflow-KT**:

### MVI Style

{% alert 'info' :: compileAs('md') %}
MVI Style refers to the general API of the library: Redux-style sends discrete objects to the library and uses some kind
of transformer class to split out the objects into discrete streams for each input type. Additionally, a true Redux 
style only transforms state, with mapper functions receiving the current state and returning the updated state, 
typically called a reducer (`(State, Input)->State`).

The MVVM+ style discards the discrete input classes, and instead offers helper functions within the ViewModel to
translate function calls on the ViewModel into lambdas that are processed in the expected MVI manner. MVVM+ typically
offers a richer API, more functionality, and reduced boilerplate, but makes it less obvious what's actually going on
within the library.
{% endalert %}

- **Ballast**: Redux-style discrete Inputs with MVVM+ style DSL
- **Redux**: Redux
- **Orbit**: MVVM+
- **MVIKotlin**: Redux
- **Uniflow-KT**: MVVM+

### Kotlin Multiplatform Support

{% alert 'info' :: compileAs('md') %}
Whether this library is available for Kotlin Multiplatform, or is limited to a single platform.
{% endalert %}

- **Ballast**: <i class="fas fa-check-double" style="color: green"></i>
- **Redux**: <i class="fas fa-ban" style="color: red"></i>
- **Orbit**: <i class="fas fa-check-double" style="color: green"></i>
- **MVIKotlin**: <i class="fas fa-check-double" style="color: green"></i>
- **Uniflow-KT**: <i class="fas fa-ban" style="color: red"></i>

### Opinionated structure

{% alert 'info' :: compileAs('md') %}
MVI is a lert lightweight design pattern overall, not really mandaing much in terms of classes, naming conventions, etc. 
But being so lightweight can make it difficult to get started if you're not comfortable with the MVI model, so it can be
helpful to have a library be opinionated about how it should be used, so you can more easily copy-and-paste code 
snippets to make it easier to try out on your own.
{% endalert %}

- **Ballast**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;
- **Redux**: <i class="fas fa-check" style="color: purple"></i>&nbsp;`createSlice()` in Redux Toolkit defines an opinionated structure
- **Orbit**: <i class="fas fa-ban" style="color: red"></i>&nbsp;Intentionally unopinionated. "MVI without the baggage. It's so simple we think of it as MVVM+"
- **MVIKotlin**: <i class="fas fa-ban" style="color: red"></i>&nbsp;
- **Uniflow-KT**: <i class="fas fa-ban" style="color: red"></i>&nbsp;Intentionally unopinionated

### Reduced boilerplate

{% alert 'info' :: compileAs('md') %}
With the MVI model comes a fair amount of boilerplate. Between creating the ViewModel/Store, defining the contract for
your State and Intents, and wiring everything up in your application code, it can be a bit overwheling. This section
shows how each library attempts to wrangle that boilerplate and make it more approachable for new users, and less 
tedious for long-time users.
{% endalert %}

- **Ballast**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;Templates/scaffolds available in [Official IntelliJ Plugin][11]
- **Redux**: <i class="fas fa-check" style="color: purple"></i>&nbsp;`createSlice()` in Redux Toolkit reduces boilerplate
- **Orbit**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;The whole framework was created to reduce boilerplate
- **MVIKotlin**: <i class="fas fa-ban" style="color: red"></i>&nbsp;
- **Uniflow-KT**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;The whole framework was created to reduce boilerplate

## State

### Reactive State

{% alert 'info' :: compileAs('md') %}
All state management libraries have a way to observe states, and this shows the function calls needed to subscribe to 
that state.
{% endalert %}

- **Ballast**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;`vm.observeStates()`
- **Redux**: <i class="fas fa-minus-circle" style="color: #ebc634"></i>&nbsp;`store.subscribe()` or 3rd-party libraries
- **Orbit**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;`container.stateFlow`
- **MVIKotlin**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;`store.states(Observer<State>)`
- **Uniflow-KT**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;`onStates(viewModel) { }`

### Get State Snapshot

{% alert 'info' :: compileAs('md') %}
Since MVI is by nature reactive, not all libraries offer an option to just query it for the current state at a given 
point in time. This section shows how to get a state snapshot if it is available.
{% endalert %}

- **Ballast**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;`vm.observeStates().value`
- **Redux**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;`store.getState()`
- **Orbit**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;`container.stateFlow.value`
- **MVIKotlin**: <i class="fas fa-check-double" style="color: green"></i>
- **Uniflow-KT**: <i class="fas fa-ban" style="color: red"></i>

### State Immutability

{% alert 'info' :: compileAs('md') %}
One of the big requirements for the MVI model to work properly is an immutable state class. If you can mutate the 
properties of the state in any way other than dispatching an Intent, then the whole model breaks down. This section 
explains how each library achieves immutability.
{% endalert %}

- **Ballast**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;Built-in with Kotlin data class
- **Redux**: <i class="fas fa-check" style="color: purple"></i>&nbsp;Requires Redux Toolkit w/ Immer
- **Orbit**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;Built-in with Kotlin data class
- **MVIKotlin**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;Built-in with Kotlin data class
- **Uniflow-KT**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;Built-in with Kotlin data class

### Update State

{% alert 'info' :: compileAs('md') %}
This section shows the DSL methods used to update the state. Redux-style updates the state as part of the Reducer's 
function signature, which always returns the updated state. MVVM+ style provides a privileged scope during the handling 
of an Intent, which allows you to call a method to update the state.
{% endalert %}

- **Ballast**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;`updateState { }`
- **Redux**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;Reducers
- **Orbit**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;`reduce { }`
- **MVIKotlin**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;`Reducer<State, Intent>`
- **Uniflow-KT**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;`setState { }`

### Restore Saved States

{% alert 'info' :: compileAs('md') %}
Sometimes you may need to destroy and recreate a ViewModel, and it is convenient to have a way to restore the previous
state of that ViewModel without needing to do a full data refresh. This shows how this could be achieved with each 
library.
{% endalert %}

- **Ballast**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;[Saved State module][14]
- **Redux**: <i class="fas fa-ban" style="color: red"></i>&nbsp;
- **Orbit**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;Built-in
- **MVIKotlin**: <i class="fas fa-minus-circle" style="color: #ebc634"></i>&nbsp;Manual restoration with Essenty
- **Uniflow-KT**: <i class="fas fa-minus-circle" style="color: #ebc634"></i>&nbsp;Only supports Android `SavedStateHandle`

#### Lifecycle Support

{% alert 'info' :: compileAs('md') %}
Applications usually have some concept of a "lifecycle", where screens, scopes, and other features are constructed and 
torn down automatically by the framework. Ideally, you'd like your ViewModels to respect that lifecycle and prevent 
changes from being sent to the UI when it is not able to receive them. This section shows how you would tie your
ViewModel's valid lifetime into the platform's Lifecycle.
{% endalert %}

- **Ballast**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;Controlled by CoroutineScope
- **Redux**: <i class="fas fa-ban" style="color: red"></i>&nbsp;
- **Orbit**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;Controlled by Android ViewModel
- **MVIKotlin**: <i class="fas fa-minus-circle" style="color: #ebc634"></i>&nbsp;Manual control with Essenty/Binder utilities
- **Uniflow-KT**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;Controlled by Android ViewModel

## Automatic View-Binding

{% alert 'info' :: compileAs('md') %}
One can naively understand the MVI model as a way to automatically apply data to the UI. In reality this description
is more accurate to the MVVM model, but regardless, some libraries offer specificly-tailed integrations into the UI
to reduce boilerplate and blur the line between MVVM and MVI.
{% endalert %}

- **Ballast**: <i class="fas fa-ban" style="color: red"></i>&nbsp;Views observe State directly
- **Redux**: <i class="fas fa-check" style="color: purple"></i>&nbsp;Integrates very well with React
- **Orbit**: <i class="fas fa-ban" style="color: red"></i>&nbsp;Views observe State directly
- **MVIKotlin**: <i class="fas fa-minus-circle" style="color: #ebc634"></i>&nbsp;Optional `MviView` utility
- **Uniflow-KT**: <i class="fas fa-ban" style="color: red"></i>&nbsp;Views observe State directly

## Non-UI State Management

{% alert 'info' :: compileAs('md') %}
State Management at its core is not concerned about UI, it's just concerned about data. And there's a lot of other data
in your application that would do well to be managed in the same way as your UI state. This section shows which 
libraries have special support or documentation for managing non-UI state.
{% endalert %}

- **Ballast**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;[Repository module][13]
- **Redux**: <i class="fas fa-ban" style="color: red"></i>&nbsp;
- **Orbit**: <i class="fas fa-ban" style="color: red"></i>&nbsp;
- **MVIKotlin**: <i class="fas fa-ban" style="color: red"></i>&nbsp;
- **Uniflow-KT**: <i class="fas fa-ban" style="color: red"></i>&nbsp;

## Intents

### Create Intent

{% alert 'info' :: compileAs('md') %}
Some MVI libraries have strict rules around creating Intents, while others are a bit more relaxes, or maybe even handle
everything internally. This section shows how to create an Intent object.
{% endalert %}

- **Ballast**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;Input sealed subclass constructor
- **Redux**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;"actionCreators" functions
- **Orbit**: <i class="fas fa-minus-circle" style="color: #ebc634"></i>&nbsp;Implicit, `fun vmAction() = intent { }`
- **MVIKotlin**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;Input sealed subclass constructor
- **Uniflow-KT**: <i class="fas fa-minus-circle" style="color: #ebc634"></i>&nbsp;Implicit, `fun vmAction = action { }`

### Send Intent to VM

{% alert 'info' :: compileAs('md') %}
This shows how one would dispatch an Intent into the library for eventual processing.
{% endalert %}

- **Ballast**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;`vm.send(Input)`/`vm.trySend(Input)`
- **Redux**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;`store.dispatch()`
- **Orbit**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;Directly call VM function
- **MVIKotlin**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;`store.accept(Intent)`
- **Uniflow-KT**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;Directly call VM function

## Asynchronous processing

### Async Foreground Computation

{% alert 'info' :: compileAs('md') %}
Foreground computations block the Intent processing queue, allowing long-running work to be completed and then directly
update the state before another Intent starts processing.
{% endalert %}

- **Ballast**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;Built-in with Coroutines
- **Redux**: <i class="fas fa-ban" style="color: red"></i>&nbsp;
- **Orbit**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;Built-in with Coroutines
- **MVIKotlin**: <i class="fas fa-ban" style="color: red"></i>&nbsp;
- **Uniflow-KT**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;Built-in with Coroutines

### Async Background Computation

{% alert 'info' :: compileAs('md') %}
Background computations do not block the main Intent queue and run in parallel to the ViewModel, but also cannot 
directly update the state. Background jobs run in parallel to the ViewModel and send their own Intents, which will get 
processed just as if the Intent were generated by the user. 

Background computations should also be bound by the same lifecycle as the ViewModel (if supported), so that these jobs
do not leak and continue running beyond the ViewModel's ability to process the changes it submits.
{% endalert %}

- **Ballast**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;`sideJob(key) { }`
- **Redux**: <i class="fas fa-check" style="color: purple"></i>&nbsp;"Thunk" middleware
- **Orbit**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;`repeatOnSubscription { }`
- **MVIKotlin**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;Executors+Messages
- **Uniflow-KT**: <i class="fas fa-minus-circle" style="color: #ebc634"></i>&nbsp;Background work launched directly in Android viewModelScope. `onFlow` utility for processing Flows

## One-Time Notifications

### Send one-off Notifications

{% alert 'info' :: compileAs('md') %}
Sending events that should only be handled once is not strictly part of the MVI model, but it can be a very useful 
feature for integrating a state management library into an older, imperative UI toolkit. This section shows how to send
these notifications from each library which supports it.
{% endalert %}

- **Ballast**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;`postEvent()`
- **Redux**: <i class="fas fa-ban" style="color: red"></i>&nbsp;
- **Orbit**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;`postSideEffect()`
- **MVIKotlin**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;`publish(Label)`
- **Uniflow-KT**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;`sendEvent()`

### React to one-off Notifications 

{% alert 'info' :: compileAs('md') %}
If the library is capable of sending one-off notifications, this section shows how to register your application to 
react to those notifications.
{% endalert %}

- **Ballast**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;`vm.attachEventHandler(EventHandler)`
- **Redux**: <i class="fas fa-ban" style="color: red"></i>&nbsp;
- **Orbit**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;`container.sideEffectFlow.collect { }`
- **MVIKotlin**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;`store.labels(Observer<Label>)`
- **Uniflow-KT**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;`onEvents(viewModel) { }`

## Additional Features

### Visual Debugging

{% alert 'info' :: compileAs('md') %}
One of the great features of the MVI model is the ability to capture states and Intents and send them elsewhere. A 
visual debugger is a great tool for capturing this activity and displaying it in a standalone UI that is not part of 
your application, so you can inspect (or even change) the data being inspected.
{% endalert %}

- **Ballast**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;Included in [IntelliJ Plugin][11]
- **Redux**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;[Official Browser Extension][21]
- **Orbit**: <i class="fas fa-ban" style="color: red"></i>&nbsp;
- **MVIKotlin**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;[Official IntelliJ Plugin][41]
- **Uniflow-KT**: <i class="fas fa-ban" style="color: red"></i>&nbsp;

### Automatic Logging

{% alert 'info' :: compileAs('md') %}
Similar to Visual Debugging, it can be helpful to print the activity of your ViewModels to the application's logs so you
can see the full history at a glance. This section shows which libraries support automatic logging, or whether you would
need to manually wrap the library to handle it yourself.
{% endalert %}

- **Ballast**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;
- **Redux**: <i class="fas fa-ban" style="color: red"></i>&nbsp;
- **Orbit**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;
- **MVIKotlin**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;
- **Uniflow-KT**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;

### Testing Framework

{% alert 'info' :: compileAs('md') %}
State management is all about managing data and doing it predictably, even though the processing is typically done
asynchronously. This section shows how one would validate that their Intents are being processed correctly.
{% endalert %}

- **Ballast**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;[Official testing library][12]
- **Redux**: <i class="fas fa-ban" style="color: red"></i>&nbsp;
- **Orbit**: <i class="fas fa-check-double" style="color: green"></i>&nbsp;[Official testing library][32]
- **MVIKotlin**: <i class="fas fa-ban" style="color: red"></i>&nbsp;
- **Uniflow-KT**: <i class="fas fa-ban" style="color: red"></i>&nbsp;

[01]: https://appmattus.medium.com/top-android-mvi-libraries-in-2021-de1afe890f27

[11]: {{ 'Ballast Debugger' | link }}
[12]: {{ 'Ballast Test' | link }}
[13]: {{ 'Ballast Repository' | link }}
[14]: {{ 'Ballast Saved State' | link }}

[20]: https://github.com/reduxjs/redux
[21]: https://github.com/reduxjs/redux-devtools
[22]: https://redux.js.org/usage/writing-tests

[30]: https://github.com/orbit-mvi/orbit-mvi
[32]: https://orbit-mvi.org/Test/overview/

[40]: https://github.com/arkivanov/MVIKotlin
[41]: https://arkivanov.github.io/MVIKotlin/time_travel.html

[50]: https://github.com/uniflow-kt/uniflow-kt
