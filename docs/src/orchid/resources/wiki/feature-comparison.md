---
---

## Feature Summary

See table below for a comparison of features or names in Ballast vs other popular MVI libraries:


| MVI Feature                               | Ballast                                                                | [Redux][20]                                                                              | [Orbit][30]                                              | [MVIKotlin][40]                                 |
|-------------------------------------------|------------------------------------------------------------------------|------------------------------------------------------------------------------------------|----------------------------------------------------------|-------------------------------------------------|
| General Philosophy                        | Opinionated Application State Management framework for all KMP targets | Lightweight JS UI State Management library, with many official and unofficial extensions | Fully-featured, low-profile UI MVI framework for Android | Redux implementation in Kotlin for Android      |
| Reactive State                            | ✅ `vm.observeStates()`                                                 | ⚠️ `store.subscribe()` or 3rd-party libraries                                            | ✅ `container.stateFlow`                                  | ✅ `store.states(Observer<State>)`               |
| Get State Snapshot                        | ✅ `vm.observeStates().value`                                           | ✅ `store.getState()`                                                                     | ✅ `container.stateFlow.value`                            |                                                 |
| State Immutability                        | ✅ Built-in with Kotlin data class                                      | ☑️ Requires Redux Toolkit w/ Immer                                                       | ✅ Built-in with Kotlin data class                        |                                                 |
| Update State                              | ✅ `updateState { }`                                                    | ✅ Reducers                                                                               | ✅ `reduce { }`                                           | `Reducer<State, Intent>`                        |
| Restore Saved States                      | ❌                                                                      | ❌                                                                                        | ✅ Built-in                                               | ⚠️ Manual restoration with Essenty              |
| Lifecycle Support                         | ✅ Controlled by CoroutineScope                                         | ❌                                                                                        | ✅ Controlled by Android ViewModel                        | ⚠️ Manual control with Essenty/Binder utilities |
| Automatic View-Binding                    | ❌ Views observe State directly                                         | ☑️ Integrates very well with React                                                       | ❌ Views observe State directly                           | ⚠️ Optional `MviView` utility                   |
| Non-UI State Management                   | ✅ [Repository Utility][13]                                             | ❌                                                                                        | ❌                                                        | ❌                                               |
| Create Intent                             | ✅ Input sealed subclass constructor                                    | ✅ "actionCreators" functions                                                             | ⚠️ Implicit, `intent { }`                                | ✅ Input sealed subclass constructor             |
| Send Intent to VM                         | ✅ `vm.send(Input)`/`vm.trySend(Input)`                                 | ✅ `store.dispatch()`                                                                     | Directly call VM function                                | ✅ `store.accept(Intent)`                        |
| Async Foreground Computation              | ✅ Built-in with Coroutines                                             | ❌                                                                                        | ✅ Built-in with Coroutines                               | ❌                                               |
| Async Background Computation              | ✅ `sideEffect(key) { }`                                                | ☑️ "Thunk" middleware                                                                    | ✅ `repeatOnSubscription { }`                             | ✅ Executors+Messages                            |
| Send one-off Notifications                | ✅ `postEvent { }`                                                      | ❌                                                                                        | ✅ `postSideEffect()`                                     | ✅ publish(Label)                                |
| React to one-off Notifications            | ✅ `vm.attachEventHandler(EventHandler)`                                | ❌                                                                                        | ✅ `container.sideEffectFlow.collect { }`                 | ✅ `store.labels(Observer<Label>)`               |
| Opinionated structure/reduced boilerplate | ⚠️ Opinionated structure requires some boilerplate                     | ☑️ `createSlice()` in Redux Toolkit                                                      | ✅ The whole framework was created to reduce boilerplate  | ❌                                               |
| Time-travel Debugging                     | ✅ [Official IntelliJ Plugin][11]                                       | ✅ [Official Browser Extension][21]                                                       | ❌                                                        | ✅ [Official IntelliJ Plugin][41]                |
| Testing Framework                         | ✅ [Official][12]                                                       | ❌                                                                                        | ✅ [Official][32]                                         | ❌                                               |

❌: Not supported
⚠️: Partially supported
✅: Fully Officially supported
☑️: Fully supported by 3rd-party 

[11]: {{ 'Debugger' | link }}
[12]: {{ 'Testing Utils' | link }}
[13]: {{ 'Repository' | link }}

[20]: https://github.com/reduxjs/redux
[21]: https://github.com/reduxjs/redux-devtools
[22]: https://redux.js.org/usage/writing-tests

[30]: https://github.com/orbit-mvi/orbit-mvi
[32]: https://orbit-mvi.org/Test/overview/

[40]: https://github.com/arkivanov/MVIKotlin
[41]: https://arkivanov.github.io/MVIKotlin/time_travel.html
