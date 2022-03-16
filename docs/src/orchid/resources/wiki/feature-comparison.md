---
---

## Feature Summary

See table below for a comparison of features or names in Ballast vs other popular MVI libraries:


| MVI Feature                               | Ballast                                                                | [Redux][20]                                                                              | [Orbit][30]                                                  | [MVIKotlin][40]                                      |
|-------------------------------------------|------------------------------------------------------------------------|------------------------------------------------------------------------------------------|--------------------------------------------------------------|------------------------------------------------------|
| General Philosophy                        | Opinionated Application State Management framework for all KMP targets | Lightweight JS UI State Management library, with many official and unofficial extensions | Fully-featured, low-profile UI MVI framework for Android     | Redux implementation in Kotlin for Android           |
| Reactive State                            | ✅&nbsp;`vm.observeStates()`                                            | ⚠️&nbsp;`store.subscribe()` or 3rd-party libraries                                       | ✅&nbsp;`container.stateFlow`                                 | ✅&nbsp;`store.states(Observer<State>)`               |
| Get State Snapshot                        | ✅&nbsp;`vm.observeStates().value`                                      | ✅&nbsp;`store.getState()`                                                                | ✅&nbsp;`container.stateFlow.value`                           | ✅&nbsp;                                              |
| State Immutability                        | ✅&nbsp;Built-in with Kotlin data class                                 | ☑️&nbsp;Requires Redux Toolkit w/ Immer                                                  | ✅&nbsp;Built-in with Kotlin data class                       | ✅&nbsp;                                              |
| Update State                              | ✅&nbsp;`updateState { }`                                               | ✅&nbsp;Reducers                                                                          | ✅&nbsp;`reduce { }`                                          | ✅&nbsp;`Reducer<State, Intent>`                      |
| Restore Saved States                      | ❌&nbsp;                                                                | ❌&nbsp;                                                                                  | ✅&nbsp;Built-in                                              | ⚠️&nbsp;Manual restoration with Essenty              |
| Lifecycle Support                         | ✅&nbsp;Controlled by CoroutineScope                                    | ❌&nbsp;                                                                                  | ✅&nbsp;Controlled by Android ViewModel                       | ⚠️&nbsp;Manual control with Essenty/Binder utilities |
| Automatic View-Binding                    | ❌&nbsp;Views observe State directly                                    | ☑️&nbsp;Integrates very well with React                                                  | ❌&nbsp;Views observe State directly                          | ⚠️&nbsp;Optional `MviView` utility                   |
| Non-UI State Management                   | ✅&nbsp;[Repository Utility][13]                                        | ❌&nbsp;                                                                                  | ❌&nbsp;                                                      | ❌&nbsp;                                              |
| Create Intent                             | ✅&nbsp;Input sealed subclass constructor                               | ✅&nbsp;"actionCreators" functions                                                        | ⚠️&nbsp;Implicit, `intent { }`                               | ✅&nbsp;Input sealed subclass constructor             |
| Send Intent to VM                         | ✅&nbsp;`vm.send(Input)`/`vm.trySend(Input)`                            | ✅&nbsp;`store.dispatch()`                                                                | ✅&nbsp;Directly call VM function                             | ✅&nbsp;`store.accept(Intent)`                        |
| Async Foreground Computation              | ✅&nbsp;Built-in with Coroutines                                        | ❌&nbsp;                                                                                  | ✅&nbsp;Built-in with Coroutines                              | ❌                                                    |
| Async Background Computation              | ✅&nbsp;`sideEffect(key) { }`                                           | ☑️&nbsp;"Thunk" middleware                                                               | ✅&nbsp;`repeatOnSubscription { }`                            | ✅&nbsp;Executors+Messages                            |
| Send one-off Notifications                | ✅&nbsp;`postEvent { }`                                                 | ❌&nbsp;                                                                                  | ✅&nbsp;`postSideEffect()`                                    | ✅&nbsp;publish(Label)                                |
| React to one-off Notifications            | ✅&nbsp;`vm.attachEventHandler(EventHandler)`                           | ❌&nbsp;                                                                                  | ✅&nbsp;`container.sideEffectFlow.collect { }`                | ✅&nbsp;`store.labels(Observer<Label>)`               |
| Opinionated structure/reduced boilerplate | ⚠️&nbsp;Opinionated structure requires some boilerplate                | ☑️&nbsp;`createSlice()` in Redux Toolkit                                                 | ✅&nbsp;The whole framework was created to reduce boilerplate | ❌&nbsp;                                              |
| Time-travel Debugging                     | ✅&nbsp;[Official IntelliJ Plugin][11]                                  | ✅&nbsp;[Official Browser Extension][21]                                                  | ❌&nbsp;                                                      | ✅&nbsp;[Official IntelliJ Plugin][41]                |
| Testing Framework                         | ✅&nbsp;[Official testing library][12]                                  | ❌&nbsp;                                                                                  | ✅&nbsp;[Official testing library][32]                        | ❌&nbsp;                                              |

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
