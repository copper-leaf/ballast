## 1.2.1 - 2022-05-18

- Don't check for cleared in trySend (fixes [#25](https://github.com/copper-leaf/ballast/issues/25))
- Improved KDoc documentation for all classes/functions in core module

## 1.2.0 - 2022-05-18

- Improvements to `AndroidViewModel`
  - Adds `attachEventHandler()` override which runs on a `CoroutineScope` rather than a `Lifecycle`, which is 
    better-suited for Compose EventHandlers
  - Adds `attachEventHandlerOnLifecycle()` to be more explicit about when the eventHandler is running on a Lifecycle
  - Original `attachEventHandler()` is now marked as deprecated, and should be replaced with 
    `attachEventHandlerOnLifecycle()`
  - Adds `observeStatesOnLifecycle()` method for more easily collecting states in XML-based Views 

## 1.1.0 - 2022-04-29

- Many improvements to the Intellij plugin
  - Allow Debugger to work with all `1.x.x` client versions
  - The Debugger's port can now be changed in the Preferences dialog
  - File templates are now provided to quickly generate components from the `Right-click > New` menu
  - The values and errors show for inputs, events, states, and sideJobs in the debugger are now more nicely formatted

## 1.0.0 - 2022-04-19

- Ballast is now considered stable with its first 1.0.0 release!
- Adds Repository `fetchWithCache` function to observe a Flow rather than emit a one-shot data source
- Adopt Flow/Coroutine adapters from KaMPKit instead of using home-grown ones

## 0.15.1 - 2022-04-08

- Fix bug in IntelliJ plugin where settings were not being saved
- Removes "sample" from IntelliJ plugin, as that example and several more are available from the documentation site
 
## 0.15.0 - 2022-04-06

- Fix several bugs in Debugger UI 
  - Kotlin coroutines versions were in conflict and crashing the debugger
  - Timestamps not correctly reported once ViewModel was refreshed
  - Connection Ballast Version not reported correctly once refreshed
- Adds new `ballast-saved-state` module
- Adds `ballast-test` API for isolating a single input
- Reverts Coroutines version back to 1.5.2. To use Ballast on iOS with the new memory model, manually include the 
  1.6.0 dependency
  
## 0.14.0 - 2022-03-28

- Attempts to get iOS support working and documented, with M1 Simulator support
- Updates Coroutines to 1.6.0, for the Native's new memory model support

## 0.13.1 - 2022-03-25

- [BUG FIX] Fix regression in debugger, where you could not view or rollback to previous States.

## 0.13.0 - 2022-03-24

- [BREAKING CHANGE] Renames `ballast-crashlytics` module to `ballast-firebase-crashlytics`
- [BREAKING CHANGE] Renames `sideEffect()` to `sideJob()` to be more accurate and less confusing in comparison to 
  other Kotlin MVI libraries.
- [BREAKING CHANGE] `BallastViewModel` no longer implements the `SendChannel` interface, it exposes the `send` and 
  `trySend` methods directly. It also adds `sendAndAwaitCompletion()` to send an Input and wait for it to completely 
  finish processing.
- [BREAKING CHANGE] The `logger` property of `BallastViewModelConfiguration.Builder` is now a function, which receives
  the viewModel name when built, to pass as the tag of a logger.
- [BREAKING CHANGE] Moves annotations for Firebase into commonMain and renames them:
  - `@FirebaseCrashlyticsInterceptor.Ignore` --> `@FirebaseCrashlyticsIgnore`
  - `@FirebaseAnalyticsInterceptor.TrackInput` --> `@FirebaseAnalyticsTrackInput`
- [FUNCTIONAL CHANGE] SideJobs are now posted to the VM immediately, rather than collected and dispatched explicitly 
  after handling. The default implementation still requires that SideJobs are sent as the last statements of the 
  `InputHandler`, though.
  - By writing a custom `InputStrategy.Guardian`, you could remove the restriction that sideJobs must be started at the
    end of the Input. This is dangerous and you shouldn't do it unless you really know what you're doing.
- [BUG FIX] Fixes issue where JS Debugger cannot connect because JS browser websocket clients can't send headers
- [NEW] Adds platform-specific `BallastLogger` implementations (`AndroidBallastLogger`, `JsConsoleBallastLogger`). The 
  default logger of `BallastViewModelConfiguration` is still `NoOpLogger()`.  

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
