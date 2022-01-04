package com.copperleaf.ballast

public interface BallastInterceptor<Inputs : Any, Events : Any, State : Any> {

// Inputs
// ---------------------------------------------------------------------------------------------------------------------

    /**
     * An Input was successfully queued in the ViewModel's Inputs channel and passed its Filter, so will be accepted
     * and passed to the Handler for processing.
     */
    public suspend fun onInputAccepted(input: Inputs) {}

    /**
     * An Input was successfully queued in the ViewModel's Inputs channel but failed to pass its Filter.
     */
    public suspend fun onInputRejected(input: Inputs) {}

    /**
     * The ViewModel's Inputs channel's buffer was full and the Input could not even be queued to be processed.
     */
    public fun onInputDropped(input: Inputs) {}

    /**
     * An Input was cancelled during processing, either due to a new input coming in while it was
     * still processing, or because the ViewModel went out of scope.
     */
    public suspend fun onInputHandledSuccessfully(input: Inputs) {}

    /**
     * An Input was cancelled during processing, either due to a new input coming in while it was
     * still processing, or because the ViewModel went out of scope.
     */
    public suspend fun onInputCancelled(input: Inputs) {}

    /**
     * A normal exception was thrown during handling of an Input. Inputs should typically handle errors in other ways,
     * but it is not considered a fatal issue that should terminate the app, since it was able to be caught by the
     * ViewModel.
     */
    public suspend fun onInputHandlerError(input: Inputs, exception: Throwable) {}

// Events
// ---------------------------------------------------------------------------------------------------------------------

    /**
     * An Event was posted to the EventHandler.
     */
    public suspend fun onEventEmitted(event: Events) {}

    /**
     * A normal exception was thrown during disptching of an Event. Events should typically handle errors in other ways,
     * but it is not considered a fatal issue that should terminate the app, since it was able to be caught by the
     * ViewModel.
     */
    public suspend fun onEventHandlerError(event: Events, exception: Throwable) {}

    /**
     * The UI EventHandler has become in a valid Lifecycle state and has started processing Events
     */
    public fun onEventProcessingStarted() {}

    /**
     * The UI EventHandler has become in an invalid Lifecycle state and has stopped processing Events
     */
    public fun onEventProcessingStopped() {}

// States
// ---------------------------------------------------------------------------------------------------------------------

    /**
     * A new State was emitted to the UI.
     */
    public suspend fun onStateEmitted(state: State) {}

// Other
// ---------------------------------------------------------------------------------------------------------------------

    /**
     * An exception was thrown somewhere within the ViewModel, but we're not quite sure from where. This is usually a
     * serious concern, and likely a candidate to terminate the app (if it didn't terminate itself already).
     */
    public fun onUnhandledError(exception: Throwable) {}
}
