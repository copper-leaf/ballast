package com.copperleaf.ballast

import com.copperleaf.ballast.internal.Status

/**
 * Notifications sent to [BallastInterceptor] to inspect the internal state of the ViewModel.
 */
public sealed class BallastNotification<Inputs : Any, Events : Any, State : Any>(
    public val viewModelType: String,
    public val viewModelName: String,
) {

// ViewModel
// ---------------------------------------------------------------------------------------------------------------------

    /**
     * The ViewModel was created and has started internal processing.
     */
    public class ViewModelStatusChanged<Inputs : Any, Events : Any, State : Any>(
        viewModelType: String,
        viewModelName: String,
        public val status: Status,
    ) : BallastNotification<Inputs, Events, State>(viewModelType, viewModelName) {
        override fun toString(): String {
            return "ViewModel status moved to: $status"
        }
    }

// Inputs
// ---------------------------------------------------------------------------------------------------------------------

    /**
     * An Input was successfully send into the input channel, but has not been filtered checked through the filter yet.
     * If the Input was queued, the input channel was not full.
     */
    public class InputQueued<Inputs : Any, Events : Any, State : Any>(
        viewModelType: String,
        viewModelName: String,
        public val input: Inputs,
    ) : BallastNotification<Inputs, Events, State>(viewModelType, viewModelName) {
        override fun toString(): String {
            return "Input Queued: $input"
        }
    }

    /**
     * An Input was successfully queued in the ViewModel's Inputs channel and passed its Filter, so will be accepted
     * and passed to the Handler for processing.
     */
    public class InputAccepted<Inputs : Any, Events : Any, State : Any>(
        viewModelType: String,
        viewModelName: String,
        public val input: Inputs,
    ) : BallastNotification<Inputs, Events, State>(viewModelType, viewModelName) {
        override fun toString(): String {
            return "Accepting input: $input"
        }
    }

    /**
     * An Input was successfully queued in the ViewModel's Inputs channel but failed to pass its Filter.
     */
    public class InputRejected<Inputs : Any, Events : Any, State : Any>(
        viewModelType: String,
        viewModelName: String,
        public val stateWhenRejected: State,
        public val input: Inputs,
    ) : BallastNotification<Inputs, Events, State>(viewModelType, viewModelName) {
        override fun toString(): String {
            return "Rejecting input: $input"
        }
    }

    /**
     * The ViewModel was cleared or its Inputs channel's buffer was full and the Input could not even be queued to be
     * processed.
     */
    public class InputDropped<Inputs : Any, Events : Any, State : Any>(
        viewModelType: String,
        viewModelName: String,
        public val input: Inputs,
    ) : BallastNotification<Inputs, Events, State>(viewModelType, viewModelName) {
        override fun toString(): String {
            return "Dropping input: $input"
        }
    }

    /**
     * An Input was cancelled during processing, either due to a new input coming in while it was
     * still processing, or because the ViewModel went out of scope.
     */
    public class InputHandledSuccessfully<Inputs : Any, Events : Any, State : Any>(
        viewModelType: String,
        viewModelName: String,
        public val input: Inputs,
    ) : BallastNotification<Inputs, Events, State>(viewModelType, viewModelName) {
        override fun toString(): String {
            return "Input handled successfully: $input"
        }
    }

    /**
     * An Input was cancelled during processing, either due to a new input coming in while it was
     * still processing, or because the ViewModel went out of scope.
     */
    public class InputCancelled<Inputs : Any, Events : Any, State : Any>(
        viewModelType: String,
        viewModelName: String,
        public val input: Inputs,
    ) : BallastNotification<Inputs, Events, State>(viewModelType, viewModelName) {
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
        viewModelType: String,
        viewModelName: String,
        public val input: Inputs,
        public val throwable: Throwable,
    ) : BallastNotification<Inputs, Events, State>(viewModelType, viewModelName) {
        override fun toString(): String {
            return "Error handling input: $input (${throwable.message})"
        }
    }

// Events
// ---------------------------------------------------------------------------------------------------------------------

    /**
     * An Event was posted to the queue to eventually be delivered to the EventHandler.
     */
    public class EventQueued<Inputs : Any, Events : Any, State : Any>(
        viewModelType: String,
        viewModelName: String,
        public val event: Events,
    ) : BallastNotification<Inputs, Events, State>(viewModelType, viewModelName) {
        override fun toString(): String {
            return "Event Queued: $event"
        }
    }

    /**
     * An Event was posted to the EventHandler.
     */
    public class EventEmitted<Inputs : Any, Events : Any, State : Any>(
        viewModelType: String,
        viewModelName: String,
        public val event: Events,
    ) : BallastNotification<Inputs, Events, State>(viewModelType, viewModelName) {
        override fun toString(): String {
            return "Emitting event: $event"
        }
    }

    /**
     * An Event was posted to the EventHandler.
     */
    public class EventHandledSuccessfully<Inputs : Any, Events : Any, State : Any>(
        viewModelType: String,
        viewModelName: String,
        public val event: Events,
    ) : BallastNotification<Inputs, Events, State>(viewModelType, viewModelName) {
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
        viewModelType: String,
        viewModelName: String,
        public val event: Events,
        public val throwable: Throwable,
    ) : BallastNotification<Inputs, Events, State>(viewModelType, viewModelName) {
        override fun toString(): String {
            return "Error handling event: $event (${throwable.message})"
        }
    }

    /**
     * The UI EventHandler has become in a valid Lifecycle state and has started processing Events
     */
    public class EventProcessingStarted<Inputs : Any, Events : Any, State : Any>(
        viewModelType: String,
        viewModelName: String,
    ) : BallastNotification<Inputs, Events, State>(viewModelType, viewModelName) {
        override fun toString(): String {
            return "Event processing started"
        }
    }

    /**
     * The UI EventHandler has become in an invalid Lifecycle state and has stopped processing Events
     */
    public class EventProcessingStopped<Inputs : Any, Events : Any, State : Any>(
        viewModelType: String,
        viewModelName: String,
    ) : BallastNotification<Inputs, Events, State>(viewModelType, viewModelName) {
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
        viewModelType: String,
        viewModelName: String,
        public val state: State,
    ) : BallastNotification<Inputs, Events, State>(viewModelType, viewModelName) {
        override fun toString(): String {
            return "State changed: $state"
        }
    }

// Side Jobs
// ---------------------------------------------------------------------------------------------------------------------

    /**
     * A sideJob was queued, but has not started yet
     */
    public class SideJobQueued<Inputs : Any, Events : Any, State : Any>(
        viewModelType: String,
        viewModelName: String,
        public val key: String,
    ) : BallastNotification<Inputs, Events, State>(viewModelType, viewModelName) {
        override fun toString(): String {
            return "sideJob queued: $key"
        }
    }

    /**
     * A sideJob was started or restarted
     */
    public class SideJobStarted<Inputs : Any, Events : Any, State : Any>(
        viewModelType: String,
        viewModelName: String,
        public val key: String,
        public val restartState: SideJobScope.RestartState,
    ) : BallastNotification<Inputs, Events, State>(viewModelType, viewModelName) {
        override fun toString(): String {
            return when (restartState) {
                SideJobScope.RestartState.Initial -> "sideJob started: $key"
                SideJobScope.RestartState.Restarted -> "sideJob restarted: $key"
            }
        }
    }

    /**
     * A sideJob ran to completion
     */
    public class SideJobCompleted<Inputs : Any, Events : Any, State : Any>(
        viewModelType: String,
        viewModelName: String,
        public val key: String,
        public val restartState: SideJobScope.RestartState,
    ) : BallastNotification<Inputs, Events, State>(viewModelType, viewModelName) {
        override fun toString(): String {
            return "sideJob finished: $key"
        }
    }

    /**
     * A sideJob was cancelled
     */
    public class SideJobCancelled<Inputs : Any, Events : Any, State : Any>(
        viewModelType: String,
        viewModelName: String,
        public val key: String,
        public val restartState: SideJobScope.RestartState,
    ) : BallastNotification<Inputs, Events, State>(viewModelType, viewModelName) {
        override fun toString(): String {
            return "sideJob cancelled: $key"
        }
    }

    /**
     * A exception was thrown inside a sideJob
     */
    public class SideJobError<Inputs : Any, Events : Any, State : Any>(
        viewModelType: String,
        viewModelName: String,
        public val key: String,
        public val restartState: SideJobScope.RestartState,
        public val throwable: Throwable,
    ) : BallastNotification<Inputs, Events, State>(viewModelType, viewModelName) {
        override fun toString(): String {
            return "Error in sideJob: $key (${throwable.message})"
        }
    }

// Interceptors
// ---------------------------------------------------------------------------------------------------------------------

    public class InterceptorAttached<Inputs : Any, Events : Any, State : Any>(
        viewModelType: String,
        viewModelName: String,
        public val interceptor: BallastInterceptor<Inputs, Events, State>,
    ) : BallastNotification<Inputs, Events, State>(viewModelType, viewModelName) {
        override fun toString(): String {
            return "Interceptor attached: $interceptor"
        }
    }

    public class InterceptorFailed<Inputs : Any, Events : Any, State : Any>(
        viewModelType: String,
        viewModelName: String,
        public val interceptor: BallastInterceptor<Inputs, Events, State>,
        public val throwable: Throwable,
    ) : BallastNotification<Inputs, Events, State>(viewModelType, viewModelName) {
        override fun toString(): String {
            return "Interceptor failed: $interceptor (${throwable.message})"
        }
    }

// Other
// ---------------------------------------------------------------------------------------------------------------------

    /**
     * An exception was thrown somewhere within the ViewModel, but we're not quite sure from where. This is usually a
     * serious concern, and likely a candidate to terminate the app (if it didn't terminate itself already).
     */
    public class UnhandledError<Inputs : Any, Events : Any, State : Any>(
        viewModelType: String,
        viewModelName: String,
        public val throwable: Throwable,
    ) : BallastNotification<Inputs, Events, State>(viewModelType, viewModelName) {
        override fun toString(): String {
            return "Uncaught error (${throwable.message})"
        }
    }
}
