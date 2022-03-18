## 0.13.0

- Renames `ballast-crashlytics` module to `ballast-firebase-crashlytics`

## 0.12.0 - 2022-03-17

- Fixes issue with `inputHandler { }` and `eventHandler { }` functions, which previously didn't actually send the input 
  or event to the lambda
- Move all InputHandler checks into the Guardian, so any/all of them could be thrown away by the end-user if needed
- make a dedicated `InputStrategyScope` to match the pattern of all other pluggable features

## 0.11.0 - 2022-03-15

- Adds "time-travel debugging" to Debugger IntelliJ plugin! Inputs can now be re-sent, and you can roll back the VM
  state to any previous point in time
- Makes configuration a builder instead of multiple constructors
- Allow BallastViewModelImpl to access the host VM, so the user-created VM is what's sent to Interceptors
- ViewModels now clear themselves when their coroutine scope is closed, rather than having to do it manually
- Adds a basic Logging facade, so all Ballast components have easy access to a common logger
- Tighten up the overall API a bit, to be more streamlined

## 0.10.0 - 2022-02-28

- Updates to Kotlin 1.6.10
- Fixes issue running IntelliJ plugin on Windows
- Starts improving the Interceptor API to eventually allow Interceptors to inject Inputs and restore States

## 0.9.0 - 2022-02-28

- Finishes Debugger UI, fully exposing all states of Inputs, Events, and SideEffects
- Converts Debugger UI to an IntelliJ Plugin, instead of a standalone desktop application
- Adds a few new `BallastNotification`s, to notify of Inputs and Events being queued, but not yet processed
- Fixed bug where SideEffect errors and cancellation was not being notified properly
- `SideEffectcope` is now also a `CoroutineScope`, so you can `launch` other coroutines directly within the 
  `sideEffect(key) { }` block without having to manually wrap it in `coroutineScope { }`

## 0.8.2 - 2022-02-23

- Fix issue with creating desktop packages.

## 0.8.1 - 2022-02-23

- Fix issue with creating desktop packages.

## 0.8.0 - 2022-02-22

This is a major release, with lots of new features, greatly improved safety, but with several significant breaking 
changes, as descibed below:

- Improves side-effects
  - They are now started through a Channel, instead of relying on a Mutex for safety
  - New `BallastInterceptor` methods added to track sideEffect starts/errors
  - SideEffects (and related APIs) now _require_ a `key` to be provided, to avoid accidental restarting cause by 
    disparate Inputs forgetting to provide a key
- Improves safety of `ParallelInputStrategy` to prevent multiple updates, rather than just throwing an error at the end
  of the Input handler block if it was used incorrectly
  - Replaces `onCompleted` callback with a `Guardian` interface, which each InputStrategy can create for itself to check
    accesses during the InputHandler, rather than at the end
- Rewrites Interceptor API
  - Instead of an interface, interceptors "events" are sent as a sealed class, `BallastNotification`. These 
    Notifications are sent to a SharedFlow inside the core ViewModel, and each Interceptor observes this SharedFlow
    from its own coroutine, launched in the viewModel coroutineScope
- Improves Repository `Cached` DSL:
  - Adds `getValueOrThrow()`
  - Adds `getCachedOrThrow()`
- Introduces Ballast Debugger, a realtime desktop app for observing what is happening within your application's Ballast
  ViewModels

## 0.7.0 - 2022-02-14

- Adds Repository module for app-wide caching and communication

## 0.6.0 - 2022-01-27

- Improves DSL for restarting SideEffects

## 0.5.0 - 2022-01-16

- Fixes issue with input Channel buffer that didn't do exactly what I expected

## 0.4.0 - 2022-01-14

- Tweaks API a bit
- SideEffects must now be the last blocks called while handling an Input, to avoid confusion about dispatching the side
  effect but not actually running until the Input processing has completed

## 0.3.0 - 2022-01-06

- Adds different strategies for accepting and processing inputs to allow for other use-cases.

- ## 0.2.0 - 2022-01-05

- Adds `ballast-test`

## 0.1.0 - 2021-11-18

- Initial Commit
