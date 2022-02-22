package com.copperleaf.ballast

/**
 * Notifications sent to [BallastInterceptor] to inspect the internal state of the ViewModel.
 */
public sealed class BallastNotification<Inputs : Any, Events : Any, State : Any>(
    public val vm: BallastViewModel<Inputs, Events, State>,
) {

// ViewModel
// ---------------------------------------------------------------------------------------------------------------------

    /**
     * The ViewModel was created and has started internal processing.
     */
    public class ViewModelStarted<Inputs : Any, Events : Any, State : Any>(
        vm: BallastViewModel<Inputs, Events, State>,
    ) : BallastNotification<Inputs, Events, State>(vm) {
        override fun toString(): String {
            return "ViewModel started: $vm"
        }
    }

    /**
     * The ViewModel was cleared.
     */
    public class ViewModelCleared<Inputs : Any, Events : Any, State : Any>(
        vm: BallastViewModel<Inputs, Events, State>,
    ) : BallastNotification<Inputs, Events, State>(vm) {
        override fun toString(): String {
            return "ViewModel cleared: $vm"
        }
    }

// Inputs
// ---------------------------------------------------------------------------------------------------------------------

    /**
     * An Input was successfully queued in the ViewModel's Inputs channel and passed its Filter, so will be accepted
     * and passed to the Handler for processing.
     */
    public class InputAccepted<Inputs : Any, Events : Any, State : Any>(
        vm: BallastViewModel<Inputs, Events, State>,
        public val input: Inputs,
    ) : BallastNotification<Inputs, Events, State>(vm) {
        override fun toString(): String {
            return "Accepting input: $input"
        }
    }

    /**
     * An Input was successfully queued in the ViewModel's Inputs channel but failed to pass its Filter.
     */
    public class InputRejected<Inputs : Any, Events : Any, State : Any>(
        vm: BallastViewModel<Inputs, Events, State>,
        public val stateWhenRejected: State,
        public val input: Inputs,
    ) : BallastNotification<Inputs, Events, State>(vm) {
        override fun toString(): String {
            return "Rejecting input: $input"
        }
    }

    /**
     * The ViewModel's Inputs channel's buffer was full and the Input could not even be queued to be processed.
     */
    public class InputDropped<Inputs : Any, Events : Any, State : Any>(
        vm: BallastViewModel<Inputs, Events, State>,
        public val input: Inputs,
    ) : BallastNotification<Inputs, Events, State>(vm) {
        override fun toString(): String {
            return "Dropping input: $input"
        }
    }

    /**
     * An Input was cancelled during processing, either due to a new input coming in while it was
     * still processing, or because the ViewModel went out of scope.
     */
    public class InputHandledSuccessfully<Inputs : Any, Events : Any, State : Any>(
        vm: BallastViewModel<Inputs, Events, State>,
        public val input: Inputs,
    ) : BallastNotification<Inputs, Events, State>(vm) {
        override fun toString(): String {
            return "Input handled successfully: $input"
        }
    }

    /**
     * An Input was cancelled during processing, either due to a new input coming in while it was
     * still processing, or because the ViewModel went out of scope.
     */
    public class InputCancelled<Inputs : Any, Events : Any, State : Any>(
        vm: BallastViewModel<Inputs, Events, State>,
        public val input: Inputs,
    ) : BallastNotification<Inputs, Events, State>(vm) {
        override fun toString(): String {
            return "Input cancelled: $input"
        }
    }

    /**
     * A normal exception was thrown during handling of an Input. Inputs should typically handle errors in other ways,
     * but it is not considered a fatal issue that should terminate the app, since it was able to be caught by the
     * ViewModel.
     */
    public class InputHandlerError<Inputs : Any, Events : Any, State : Any>(
        vm: BallastViewModel<Inputs, Events, State>,
        public val input: Inputs,
        public val throwable: Throwable,
    ) : BallastNotification<Inputs, Events, State>(vm) {
        override fun toString(): String {
            return "Error handling input: $input (${throwable.message})"
        }
    }

// Events
// ---------------------------------------------------------------------------------------------------------------------

    /**
     * An Event was posted to the EventHandler.
     */
    public class EventEmitted<Inputs : Any, Events : Any, State : Any>(
        vm: BallastViewModel<Inputs, Events, State>,
        public val event: Events,
    ) : BallastNotification<Inputs, Events, State>(vm) {
        override fun toString(): String {
            return "Emitting event: $event"
        }
    }

    /**
     * An Event was posted to the EventHandler.
     */
    public class EventHandledSuccessfully<Inputs : Any, Events : Any, State : Any>(
        vm: BallastViewModel<Inputs, Events, State>,
        public val event: Events,
    ) : BallastNotification<Inputs, Events, State>(vm) {
        override fun toString(): String {
            return "Event handled successfully: $event"
        }
    }

    /**
     * A normal exception was thrown during disptching of an Event. Events should typically handle errors in other ways,
     * but it is not considered a fatal issue that should terminate the app, since it was able to be caught by the
     * ViewModel.
     */
    public class EventHandlerError<Inputs : Any, Events : Any, State : Any>(
        vm: BallastViewModel<Inputs, Events, State>,
        public val event: Events,
        public val throwable: Throwable,
    ) : BallastNotification<Inputs, Events, State>(vm) {
        override fun toString(): String {
            return "Error handling event: $event (${throwable.message})"
        }
    }

    /**
     * The UI EventHandler has become in a valid Lifecycle state and has started processing Events
     */
    public class EventProcessingStarted<Inputs : Any, Events : Any, State : Any>(
        vm: BallastViewModel<Inputs, Events, State>,
    ) : BallastNotification<Inputs, Events, State>(vm) {
        override fun toString(): String {
            return "Event processing started"
        }
    }

    /**
     * The UI EventHandler has become in an invalid Lifecycle state and has stopped processing Events
     */
    public class EventProcessingStopped<Inputs : Any, Events : Any, State : Any>(
        vm: BallastViewModel<Inputs, Events, State>,
    ) : BallastNotification<Inputs, Events, State>(vm) {
        override fun toString(): String {
            return "Event processing stopped"
        }
    }

// States
// ---------------------------------------------------------------------------------------------------------------------

    /**
     * A new State was emitted to the UI.
     */
    public class StateChanged<Inputs : Any, Events : Any, State : Any>(
        vm: BallastViewModel<Inputs, Events, State>,
        public val state: State,
    ) : BallastNotification<Inputs, Events, State>(vm) {
        override fun toString(): String {
            return "State changed: $state"
        }
    }

// Side Effects
// ---------------------------------------------------------------------------------------------------------------------

    /**
     * A sideEffect was started or restarted
     */
    public class SideEffectStarted<Inputs : Any, Events : Any, State : Any>(
        vm: BallastViewModel<Inputs, Events, State>,
        public val key: String,
        public val restartState: SideEffectScope.RestartState,
    ) : BallastNotification<Inputs, Events, State>(vm) {
        override fun toString(): String {
            return when (restartState) {
                SideEffectScope.RestartState.Initial -> "SideEffect started: $key"
                SideEffectScope.RestartState.Restarted -> "SideEffect restarted: $key"
            }
        }
    }

    /**
     * A sideEffect was started or restarted
     */
    public class SideEffectCompleted<Inputs : Any, Events : Any, State : Any>(
        vm: BallastViewModel<Inputs, Events, State>,
        public val key: String,
    ) : BallastNotification<Inputs, Events, State>(vm) {
        override fun toString(): String {
            return "sideEffect finished: $key"
        }
    }

    /**
     * A sideEffect was started or restarted
     */
    public class SideEffectError<Inputs : Any, Events : Any, State : Any>(
        vm: BallastViewModel<Inputs, Events, State>,
        public val key: String,
        public val throwable: Throwable,
    ) : BallastNotification<Inputs, Events, State>(vm) {
        override fun toString(): String {
            return "Error in sideEffect: $key (${throwable.message})"
        }
    }

// Other
// ---------------------------------------------------------------------------------------------------------------------

    /**
     * An exception was thrown somewhere within the ViewModel, but we're not quite sure from where. This is usually a
     * serious concern, and likely a candidate to terminate the app (if it didn't terminate itself already).
     */
    public class UnhandledError<Inputs : Any, Events : Any, State : Any>(
        vm: BallastViewModel<Inputs, Events, State>,
        public val throwable: Throwable,
    ) : BallastNotification<Inputs, Events, State>(vm) {
        override fun toString(): String {
            return "Uncaught error (${throwable.message})"
        }
    }
}
