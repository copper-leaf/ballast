# Feature Comparison


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

- ✅ Fully Officially supported&nbsp;&nbsp;
- ✓ Fully supported by 3rd-party&nbsp;&nbsp;
- ⚠️ Partially supported&nbsp;&nbsp;
- ❌ Not supported&nbsp;&nbsp;

## General

### General Philosophy

> **Note:**
> This refers to the general development philosophy behind the development of the library, such as whether it's aiming
> to be lightweight or fully featured, as well as any other significant notes about how to approach the library.
- **Ballast**: Opinionated Application State Management framework for all KMP targets
- **Redux**: Lightweight JS UI State Management library, with many official and unofficial extensions
- **Orbit**: Fully-featured, low-profile UI MVI framework for Android
- **MVIKotlin**: Redux implementation in Kotlin for Android
- **Uniflow-KT**:

### MVI Style

> **Note:**
>
> MVI Style refers to the general API of the library: Redux-style sends discrete objects to the library and uses some kind
> of transformer class to split out the objects into discrete streams for each input type. Additionally, a true Redux
> style only transforms state, with mapper functions receiving the current state and returning the updated state,
> typically called a reducer (`(State, Input)->State`).
>
> The MVVM+ style discards the discrete input classes, and instead offers helper functions within the ViewModel to
> translate function calls on the ViewModel into lambdas that are processed in the expected MVI manner. MVVM+ typically
> offers a richer API, more functionality, and reduced boilerplate, but makes it less obvious what's actually going on
> within the library.
>
> - **Ballast**: Redux-style discrete Inputs with MVVM+ style DSL
> - **Redux**: Redux
> - **Orbit**: MVVM+
> - **MVIKotlin**: Redux
> - **Uniflow-KT**: MVVM+
>
> ### Kotlin Multiplatform Support
>
> > **Note:**
> > Whether this library is available for Kotlin Multiplatform, or is limited to a single platform.
> - **Ballast**: ✅
> - **Redux**: ❌
> - **Orbit**: ✅
> - **MVIKotlin**: ✅
> - **Uniflow-KT**: ❌
>
> ### Opinionated structure
>
> > **Note:**
> > MVI is a lert lightweight design pattern overall, not really mandaing much in terms of classes, naming conventions, etc.
> > But being so lightweight can make it difficult to get started if you're not comfortable with the MVI model, so it can be
> > helpful to have a library be opinionated about how it should be used, so you can more easily copy-and-paste code
> > snippets to make it easier to try out on your own.
> - **Ballast**: ✅
> - **Redux**: ✓ `createSlice()` in Redux Toolkit defines an opinionated structure
> - **Orbit**: ❌ Intentionally unopinionated. "MVI without the baggage. It's so simple we think of it as MVVM+"
> - **MVIKotlin**: ❌
> - **Uniflow-KT**: ❌ Intentionally unopinionated
>
> ### Reduced boilerplate
>
> > **Note:**
> > With the MVI model comes a fair amount of boilerplate. Between creating the ViewModel/Store, defining the contract for
> > your State and Intents, and wiring everything up in your application code, it can be a bit overwheling. This section
> > shows how each library attempts to wrangle that boilerplate and make it more approachable for new users, and less
> > tedious for long-time users.
> - **Ballast**: ✅ Templates/scaffolds available in [Official IntelliJ Plugin][11]
> - **Redux**: ✓ `createSlice()` in Redux Toolkit reduces boilerplate
> - **Orbit**: ✅ The whole framework was created to reduce boilerplate
> - **MVIKotlin**: ❌
> - **Uniflow-KT**: ✅ The whole framework was created to reduce boilerplate
>
> ## State
>
> ### Reactive State
>
> > **Note:**
> > All state management libraries have a way to observe states, and this shows the function calls needed to subscribe to
> > that state.
> - **Ballast**: ✅ `vm.observeStates()`
> - **Redux**: ⚠️ `store.subscribe()` or 3rd-party libraries
> - **Orbit**: ✅ `container.stateFlow`
> - **MVIKotlin**: ✅ `store.states(Observer<State>)`
> - **Uniflow-KT**: ✅ `onStates(viewModel) { }`
>
> ### Get State Snapshot
>
> > **Note:**
> > Since MVI is by nature reactive, not all libraries offer an option to just query it for the current state at a given
> > point in time. This section shows how to get a state snapshot if it is available.
> - **Ballast**: ✅ `vm.observeStates().value`
> - **Redux**: ✅ `store.getState()`
> - **Orbit**: ✅ `container.stateFlow.value`
> - **MVIKotlin**: ✅
> - **Uniflow-KT**: ❌
>
> ### State Immutability
>
> > **Note:**
> > One of the big requirements for the MVI model to work properly is an immutable state class. If you can mutate the
> > properties of the state in any way other than dispatching an Intent, then the whole model breaks down. This section
> > explains how each library achieves immutability.
> - **Ballast**: ✅ Built-in with Kotlin data class
> - **Redux**: ✓ Requires Redux Toolkit w/ Immer
> - **Orbit**: ✅ Built-in with Kotlin data class
> - **MVIKotlin**: ✅ Built-in with Kotlin data class
> - **Uniflow-KT**: ✅ Built-in with Kotlin data class
>
> ### Update State
>
> > **Note:**
> > This section shows the DSL methods used to update the state. Redux-style updates the state as part of the Reducer's
> > function signature, which always returns the updated state. MVVM+ style provides a privileged scope during the handling
> > of an Intent, which allows you to call a method to update the state.
> - **Ballast**: ✅ `updateState { }`
> - **Redux**: ✅ Reducers
> - **Orbit**: ✅ `reduce { }`
> - **MVIKotlin**: ✅ `Reducer<State, Intent>`
> - **Uniflow-KT**: ✅ `setState { }`
>
> ### Restore Saved States
>
> > **Note:**
> > Sometimes you may need to destroy and recreate a ViewModel, and it is convenient to have a way to restore the previous
> > state of that ViewModel without needing to do a full data refresh. This shows how this could be achieved with each
> > library.
> - **Ballast**: ✅ [Saved State module][14]
> - **Redux**: ❌
> - **Orbit**: ✅ Built-in
> - **MVIKotlin**: ⚠️ Manual restoration with Essenty
> - **Uniflow-KT**: ⚠️ Only supports Android `SavedStateHandle`
>
> #### Lifecycle Support
>
> > **Note:**
> > Applications usually have some concept of a "lifecycle", where screens, scopes, and other features are constructed and
> > torn down automatically by the framework. Ideally, you'd like your ViewModels to respect that lifecycle and prevent
> > changes from being sent to the UI when it is not able to receive them. This section shows how you would tie your
> > ViewModel's valid lifetime into the platform's Lifecycle.
> - **Ballast**: ✅ Controlled by CoroutineScope
> - **Redux**: ❌
> - **Orbit**: ✅ Controlled by Android ViewModel
> - **MVIKotlin**: ⚠️ Manual control with Essenty/Binder utilities
> - **Uniflow-KT**: ✅ Controlled by Android ViewModel
>
> ## Automatic View-Binding
>
> > **Note:**
> > One can naively understand the MVI model as a way to automatically apply data to the UI. In reality this description
> > is more accurate to the MVVM model, but regardless, some libraries offer specificly-tailed integrations into the UI
> > to reduce boilerplate and blur the line between MVVM and MVI.
> - **Ballast**: ❌ Views observe State directly
> - **Redux**: ✓ Integrates very well with React
> - **Orbit**: ❌ Views observe State directly
> - **MVIKotlin**: ⚠️ Optional `MviView` utility
> - **Uniflow-KT**: ❌ Views observe State directly
>
> ## Non-UI State Management
>
> > **Note:**
> > State Management at its core is not concerned about UI, it's just concerned about data. And there's a lot of other data
> > in your application that would do well to be managed in the same way as your UI state. This section shows which
> > libraries have special support or documentation for managing non-UI state.
> - **Ballast**: ✅ [Repository module][13]
> - **Redux**: ❌
> - **Orbit**: ❌
> - **MVIKotlin**: ❌
> - **Uniflow-KT**: ❌
>
> ## Intents
>
> ### Create Intent
>
> > **Note:**
> > Some MVI libraries have strict rules around creating Intents, while others are a bit more relaxes, or maybe even handle
> > everything internally. This section shows how to create an Intent object.
> - **Ballast**: ✅ Input sealed subclass constructor
> - **Redux**: ✅ "actionCreators" functions
> - **Orbit**: ⚠️ Implicit, `fun vmAction() = intent { }`
> - **MVIKotlin**: ✅ Input sealed subclass constructor
> - **Uniflow-KT**: ⚠️ Implicit, `fun vmAction = action { }`
>
> ### Send Intent to VM
>
> > **Note:**
> > This shows how one would dispatch an Intent into the library for eventual processing.
> - **Ballast**: ✅ `vm.send(Input)`/`vm.trySend(Input)`
> - **Redux**: ✅ `store.dispatch()`
> - **Orbit**: ✅ Directly call VM function
> - **MVIKotlin**: ✅ `store.accept(Intent)`
> - **Uniflow-KT**: ✅ Directly call VM function
>
> ## Asynchronous processing
>
> ### Async Foreground Computation
>
> > **Note:**
> > Foreground computations block the Intent processing queue, allowing long-running work to be completed and then directly
> > update the state before another Intent starts processing.
> - **Ballast**: ✅ Built-in with Coroutines
> - **Redux**: ❌
> - **Orbit**: ✅ Built-in with Coroutines
> - **MVIKotlin**: ❌
> - **Uniflow-KT**: ✅ Built-in with Coroutines
>
> ### Async Background Computation
>
> > **Note:**
> > Background computations do not block the main Intent queue and run in parallel to the ViewModel, but also cannot
> > directly update the state. Background jobs run in parallel to the ViewModel and send their own Intents, which will get
> > processed just as if the Intent were generated by the user.
> >
> > Background computations should also be bound by the same lifecycle as the ViewModel (if supported), so that these jobs
> > do not leak and continue running beyond the ViewModel's ability to process the changes it submits.
> - **Ballast**: ✅ `sideJob(key) { }`
> - **Redux**: ✓ "Thunk" middleware
> - **Orbit**: ✅ `repeatOnSubscription { }`
> - **MVIKotlin**: ✅ Executors+Messages
> - **Uniflow-KT**: ⚠️ Background work launched directly in Android viewModelScope. `onFlow` utility for processing Flows
>
> ## One-Time Notifications
>
> ### Send one-off Notifications
>
> > **Note:**
> > Sending events that should only be handled once is not strictly part of the MVI model, but it can be a very useful
> > feature for integrating a state management library into an older, imperative UI toolkit. This section shows how to send
> > these notifications from each library which supports it.
> - **Ballast**: ✅ `postEvent()`
> - **Redux**: ❌
> - **Orbit**: ✅ `postSideEffect()`
> - **MVIKotlin**: ✅ `publish(Label)`
> - **Uniflow-KT**: ✅ `sendEvent()`
>
> ### React to one-off Notifications
>
> > **Note:**
> > If the library is capable of sending one-off notifications, this section shows how to register your application to
> > react to those notifications.
> - **Ballast**: ✅ `vm.attachEventHandler(EventHandler)`
> - **Redux**: ❌
> - **Orbit**: ✅ `container.sideEffectFlow.collect { }`
> - **MVIKotlin**: ✅ `store.labels(Observer<Label>)`
> - **Uniflow-KT**: ✅ `onEvents(viewModel) { }`
>
> ## Additional Features
>
> ### Visual Debugging
>
> > **Note:**
> > One of the great features of the MVI model is the ability to capture states and Intents and send them elsewhere. A
> > visual debugger is a great tool for capturing this activity and displaying it in a standalone UI that is not part of
> > your application, so you can inspect (or even change) the data being inspected.
> - **Ballast**: ✅ Included in [IntelliJ Plugin][11]
> - **Redux**: ✅ [Official Browser Extension][21]
> - **Orbit**: ❌
> - **MVIKotlin**: ✅ [Official IntelliJ Plugin][41]
> - **Uniflow-KT**: ❌
>
> ### Automatic Logging
>
> > **Note:**
> > Similar to Visual Debugging, it can be helpful to print the activity of your ViewModels to the application's logs so you
> > can see the full history at a glance. This section shows which libraries support automatic logging, or whether you would
> > need to manually wrap the library to handle it yourself.
> - **Ballast**: ✅
> - **Redux**: ❌
> - **Orbit**: ✅
> - **MVIKotlin**: ✅
> - **Uniflow-KT**: ✅
>
> ### Testing Framework
>
> > **Note:**
> > State management is all about managing data and doing it predictably, even though the processing is typically done
> > asynchronously. This section shows how one would validate that their Intents are being processed correctly.
> - **Ballast**: ✅ [Official testing library][12]
> - **Redux**: ❌
> - **Orbit**: ✅ [Official testing library][32]
> - **MVIKotlin**: ❌
> - **Uniflow-KT**: ❌
>
> [01]: https://appmattus.medium.com/top-android-mvi-libraries-in-2021-de1afe890f27
>
> [11]: ../ballast-idea-plugin/
> [12]: ../ballast-test/
> [13]: ../ballast-repository/
> [14]: ../ballast-saved-state/
>
> [20]: https://github.com/reduxjs/redux
> [21]: https://github.com/reduxjs/redux-devtools
> [22]: https://redux.js.org/usage/writing-tests
>
> [30]: https://github.com/orbit-mvi/orbit-mvi
> [32]: https://orbit-mvi.org/Test/overview/
>
> [40]: https://github.com/arkivanov/MVIKotlin
> [41]: https://arkivanov.github.io/MVIKotlin/time_travel.html
>
> [50]: https://github.com/uniflow-kt/uniflow-kt