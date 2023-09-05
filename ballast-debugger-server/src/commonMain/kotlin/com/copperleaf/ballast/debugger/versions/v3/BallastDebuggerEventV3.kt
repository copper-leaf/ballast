package com.copperleaf.ballast.debugger.versions.v3

import com.copperleaf.ballast.SideJobScope
import com.copperleaf.ballast.debugger.models.BallastLocalDateTimeSerializer
import com.copperleaf.ballast.debugger.utils.now
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

private const val EVENT_MODEL_BASE_CLASS_NAME = "com.copperleaf.ballast.debugger.versions.v3.BallastDebuggerEvent"

@Serializable
public sealed class BallastDebuggerEventV3 {
    public abstract val connectionId: String
    public abstract val viewModelName: String?
    public abstract val uuid: String?

    @Serializable(with = BallastLocalDateTimeSerializer::class)
    public abstract val timestamp: LocalDateTime

// Events not from Interceptor, but used for app internal communication
// ---------------------------------------------------------------------------------------------------------------------

    /**
     * Send a heartbeat from the device so the UI can detect when it is active and when the connection has died
     */
    @Serializable
    @SerialName("$EVENT_MODEL_BASE_CLASS_NAME.Heartbeat")
    public class Heartbeat(
        override val connectionId: String,
        public val connectionBallastVersion: String,
    ) : BallastDebuggerEventV3() {
        override val viewModelName: String? = null
        override val uuid: String? = null

        @Serializable(with = BallastLocalDateTimeSerializer::class)
        override val timestamp: LocalDateTime = LocalDateTime.now()
    }

    /**
     * Clear all data in the given ViewModel, so the device can re-send the entire history and bring it back to the
     * proper status in the UI, and set the flag to denote that a full refresh is underway.
     */
    @Serializable
    @SerialName("$EVENT_MODEL_BASE_CLASS_NAME.RefreshViewModelStart")
    public class RefreshViewModelStart(
        override val connectionId: String,
        override val viewModelName: String,
    ) : BallastDebuggerEventV3() {
        override val uuid: String? = null

        @Serializable(with = BallastLocalDateTimeSerializer::class)
        override val timestamp: LocalDateTime = LocalDateTime.now()
    }

    /**
     * Clear the flag to denote that a full refresh is underway.
     */
    @Serializable
    @SerialName("$EVENT_MODEL_BASE_CLASS_NAME.RefreshViewModelComplete")
    public class RefreshViewModelComplete(
        override val connectionId: String,
        override val viewModelName: String,
    ) : BallastDebuggerEventV3() {
        override val uuid: String? = null

        @Serializable(with = BallastLocalDateTimeSerializer::class)
        override val timestamp: LocalDateTime = LocalDateTime.now()
    }

// ViewModel
// ---------------------------------------------------------------------------------------------------------------------

    @Serializable
    @SerialName("$EVENT_MODEL_BASE_CLASS_NAME.ViewModelStarted")
    public class ViewModelStatusChanged(
        override val connectionId: String,
        override val viewModelName: String,
        public val viewModelType: String,
        override val uuid: String,
        @Serializable(with = BallastLocalDateTimeSerializer::class)
        override val timestamp: LocalDateTime,
        public val status: StatusV3,
    ) : BallastDebuggerEventV3() {
        override fun toString(): String {
            return "ViewModel status moved to: $status"
        }
    }


// Inputs
// ---------------------------------------------------------------------------------------------------------------------

    @Serializable
    @SerialName("$EVENT_MODEL_BASE_CLASS_NAME.InputQueued")
    public class InputQueued(
        override val connectionId: String,
        override val viewModelName: String,
        override val uuid: String,
        @Serializable(with = BallastLocalDateTimeSerializer::class)
        override val timestamp: LocalDateTime,

        public val inputType: String,
        public val serializedInput: String,
        public val inputContentType: String,
    ) : BallastDebuggerEventV3() {
        override fun toString(): String {
            return "Input Queued: $serializedInput"
        }
    }

    @Serializable
    @SerialName("$EVENT_MODEL_BASE_CLASS_NAME.InputAccepted")
    public class InputAccepted(
        override val connectionId: String,
        override val viewModelName: String,
        override val uuid: String,
        @Serializable(with = BallastLocalDateTimeSerializer::class)
        override val timestamp: LocalDateTime,

        public val inputType: String,
        public val serializedInput: String,
        public val inputContentType: String,
    ) : BallastDebuggerEventV3() {
        override fun toString(): String {
            return "Accepting input: $serializedInput"
        }
    }

    @Serializable
    @SerialName("$EVENT_MODEL_BASE_CLASS_NAME.InputRejected")
    public class InputRejected(
        override val connectionId: String,
        override val viewModelName: String,
        override val uuid: String,
        @Serializable(with = BallastLocalDateTimeSerializer::class)
        override val timestamp: LocalDateTime,

        public val inputType: String,
        public val serializedInput: String,
        public val inputContentType: String,
    ) : BallastDebuggerEventV3() {
        override fun toString(): String {
            return "Rejecting input: $serializedInput"
        }
    }

    @Serializable
    @SerialName("$EVENT_MODEL_BASE_CLASS_NAME.InputDropped")
    public class InputDropped(
        override val connectionId: String,
        override val viewModelName: String,
        override val uuid: String,
        @Serializable(with = BallastLocalDateTimeSerializer::class)
        override val timestamp: LocalDateTime,

        public val inputType: String,
        public val serializedInput: String,
        public val inputContentType: String,
    ) : BallastDebuggerEventV3() {
        override fun toString(): String {
            return "Dropping input: $serializedInput"
        }
    }

    @Serializable
    @SerialName("$EVENT_MODEL_BASE_CLASS_NAME.InputHandledSuccessfully")
    public class InputHandledSuccessfully(
        override val connectionId: String,
        override val viewModelName: String,
        override val uuid: String,
        @Serializable(with = BallastLocalDateTimeSerializer::class)
        override val timestamp: LocalDateTime,

        public val inputType: String,
        public val serializedInput: String,
        public val inputContentType: String,
    ) : BallastDebuggerEventV3() {
        override fun toString(): String {
            return "Input handled successfully: $serializedInput"
        }
    }

    @Serializable
    @SerialName("$EVENT_MODEL_BASE_CLASS_NAME.InputCancelled")
    public class InputCancelled(
        override val connectionId: String,
        override val viewModelName: String,
        override val uuid: String,
        @Serializable(with = BallastLocalDateTimeSerializer::class)
        override val timestamp: LocalDateTime,

        public val inputType: String,
        public val serializedInput: String,
        public val inputContentType: String,
    ) : BallastDebuggerEventV3() {
        override fun toString(): String {
            return "Input cancelled: $serializedInput"
        }
    }

    @Serializable
    @SerialName("$EVENT_MODEL_BASE_CLASS_NAME.InputHandlerError")
    public class InputHandlerError(
        override val connectionId: String,
        override val viewModelName: String,
        override val uuid: String,
        @Serializable(with = BallastLocalDateTimeSerializer::class)
        override val timestamp: LocalDateTime,

        public val inputType: String,
        public val serializedInput: String,
        public val inputContentType: String,
        public val stacktrace: String,
    ) : BallastDebuggerEventV3() {
        override fun toString(): String {
            return "Error handling input: $serializedInput\n$stacktrace"
        }
    }

// Events
// ---------------------------------------------------------------------------------------------------------------------

    @Serializable
    @SerialName("$EVENT_MODEL_BASE_CLASS_NAME.EventQueued")
    public class EventQueued(
        override val connectionId: String,
        override val viewModelName: String,
        override val uuid: String,
        @Serializable(with = BallastLocalDateTimeSerializer::class)
        override val timestamp: LocalDateTime,

        public val eventType: String,
        public val serializedEvent: String,
        public val eventContentType: String,
    ) : BallastDebuggerEventV3() {
        override fun toString(): String {
            return "Event Queued: $serializedEvent"
        }
    }

    @Serializable
    @SerialName("$EVENT_MODEL_BASE_CLASS_NAME.EventEmitted")
    public class EventEmitted(
        override val connectionId: String,
        override val viewModelName: String,
        override val uuid: String,
        @Serializable(with = BallastLocalDateTimeSerializer::class)
        override val timestamp: LocalDateTime,

        public val eventType: String,
        public val serializedEvent: String,
        public val eventContentType: String,
    ) : BallastDebuggerEventV3() {
        override fun toString(): String {
            return "Emitting event: $serializedEvent"
        }
    }

    @Serializable
    @SerialName("$EVENT_MODEL_BASE_CLASS_NAME.EventHandledSuccessfully")
    public class EventHandledSuccessfully(
        override val connectionId: String,
        override val viewModelName: String,
        override val uuid: String,
        @Serializable(with = BallastLocalDateTimeSerializer::class)
        override val timestamp: LocalDateTime,

        public val eventType: String,
        public val serializedEvent: String,
        public val eventContentType: String,
    ) : BallastDebuggerEventV3() {
        override fun toString(): String {
            return "Event handled successfully: $serializedEvent"
        }
    }

    @Serializable
    @SerialName("$EVENT_MODEL_BASE_CLASS_NAME.EventHandlerError")
    public class EventHandlerError(
        override val connectionId: String,
        override val viewModelName: String,
        override val uuid: String,
        @Serializable(with = BallastLocalDateTimeSerializer::class)
        override val timestamp: LocalDateTime,

        public val eventType: String,
        public val serializedEvent: String,
        public val eventContentType: String,
        public val stacktrace: String,
    ) : BallastDebuggerEventV3() {
        override fun toString(): String {
            return "Error handling event: $serializedEvent\n$stacktrace)"
        }
    }

    @Serializable
    @SerialName("$EVENT_MODEL_BASE_CLASS_NAME.EventProcessingStarted")
    public class EventProcessingStarted(
        override val connectionId: String,
        override val viewModelName: String,
        override val uuid: String,
        @Serializable(with = BallastLocalDateTimeSerializer::class)
        override val timestamp: LocalDateTime,
    ) : BallastDebuggerEventV3() {
        override fun toString(): String {
            return "Event processing started"
        }
    }

    @Serializable
    @SerialName("$EVENT_MODEL_BASE_CLASS_NAME.EventProcessingStopped")
    public class EventProcessingStopped(
        override val connectionId: String,
        override val viewModelName: String,
        override val uuid: String,
        @Serializable(with = BallastLocalDateTimeSerializer::class)
        override val timestamp: LocalDateTime,
    ) : BallastDebuggerEventV3() {
        override fun toString(): String {
            return "Event processing stopped"
        }
    }

// States
// ---------------------------------------------------------------------------------------------------------------------

    @Serializable
    @SerialName("$EVENT_MODEL_BASE_CLASS_NAME.StateChanged")
    public class StateChanged(
        override val connectionId: String,
        override val viewModelName: String,
        override val uuid: String,
        @Serializable(with = BallastLocalDateTimeSerializer::class)
        override val timestamp: LocalDateTime,

        public val stateType: String,
        public val serializedState: String,
        public val stateContentType: String,
    ) : BallastDebuggerEventV3() {
        override fun toString(): String {
            return "State changed: $serializedState"
        }
    }

// Side-jobs
// ---------------------------------------------------------------------------------------------------------------------

    @Serializable
    @SerialName("$EVENT_MODEL_BASE_CLASS_NAME.SideJobQueued")
    public class SideJobQueued(
        override val connectionId: String,
        override val viewModelName: String,
        override val uuid: String,
        @Serializable(with = BallastLocalDateTimeSerializer::class)
        override val timestamp: LocalDateTime,

        public val key: String,
    ) : BallastDebuggerEventV3() {
        override fun toString(): String {
            return "sideJob queued: $key"
        }
    }

    @Serializable
    @SerialName("$EVENT_MODEL_BASE_CLASS_NAME.SideJobStarted")
    public class SideJobStarted(
        override val connectionId: String,
        override val viewModelName: String,
        override val uuid: String,
        @Serializable(with = BallastLocalDateTimeSerializer::class)
        override val timestamp: LocalDateTime,

        public val key: String,
        public val restartState: SideJobScope.RestartState,
    ) : BallastDebuggerEventV3() {
        override fun toString(): String {
            return when (restartState) {
                SideJobScope.RestartState.Initial -> "sideJob started: $key"
                SideJobScope.RestartState.Restarted -> "sideJob restarted: $key"
            }
        }
    }

    @Serializable
    @SerialName("$EVENT_MODEL_BASE_CLASS_NAME.SideJobCompleted")
    public class SideJobCompleted(
        override val connectionId: String,
        override val viewModelName: String,
        override val uuid: String,
        @Serializable(with = BallastLocalDateTimeSerializer::class)
        override val timestamp: LocalDateTime,

        public val key: String,
        public val restartState: SideJobScope.RestartState,
    ) : BallastDebuggerEventV3() {
        override fun toString(): String {
            return "sideJob finished: $key"
        }
    }

    @Serializable
    @SerialName("$EVENT_MODEL_BASE_CLASS_NAME.SideJobCancelled")
    public class SideJobCancelled(
        override val connectionId: String,
        override val viewModelName: String,
        override val uuid: String,
        @Serializable(with = BallastLocalDateTimeSerializer::class)
        override val timestamp: LocalDateTime,

        public val key: String,
        public val restartState: SideJobScope.RestartState,
    ) : BallastDebuggerEventV3() {
        override fun toString(): String {
            return "sideJob cancelled: $key"
        }
    }

    @Serializable
    @SerialName("$EVENT_MODEL_BASE_CLASS_NAME.SideJobError")
    public class SideJobError(
        override val connectionId: String,
        override val viewModelName: String,
        override val uuid: String,
        @Serializable(with = BallastLocalDateTimeSerializer::class)
        override val timestamp: LocalDateTime,

        public val key: String,
        public val restartState: SideJobScope.RestartState,
        public val stacktrace: String,
    ) : BallastDebuggerEventV3() {
        override fun toString(): String {
            return "Error in sideJob: $key\n$stacktrace"
        }
    }

// Interceptors
// ---------------------------------------------------------------------------------------------------------------------

    @Serializable
    @SerialName("$EVENT_MODEL_BASE_CLASS_NAME.InterceptorAttached")
    public class InterceptorAttached(
        override val connectionId: String,
        override val viewModelName: String,
        override val uuid: String,
        @Serializable(with = BallastLocalDateTimeSerializer::class)
        override val timestamp: LocalDateTime,

        public val interceptorType: String,
        public val interceptorToStringValue: String,
    ) : BallastDebuggerEventV3() {
        override fun toString(): String {
            return "Interceptor attached: $interceptorType"
        }
    }

    @Serializable
    @SerialName("$EVENT_MODEL_BASE_CLASS_NAME.InterceptorFailed")
    public class InterceptorFailed(
        override val connectionId: String,
        override val viewModelName: String,
        override val uuid: String,
        @Serializable(with = BallastLocalDateTimeSerializer::class)
        override val timestamp: LocalDateTime,

        public val interceptorType: String,
        public val interceptorToStringValue: String,
        public val stacktrace: String,
    ) : BallastDebuggerEventV3() {
        override fun toString(): String {
            return "Interceptor failed: $interceptorType\n$stacktrace"
        }
    }

// Other
// ---------------------------------------------------------------------------------------------------------------------

    @Serializable
    @SerialName("$EVENT_MODEL_BASE_CLASS_NAME.UnhandledError")
    public class UnhandledError(
        override val connectionId: String,
        override val viewModelName: String,
        override val uuid: String,
        @Serializable(with = BallastLocalDateTimeSerializer::class)
        override val timestamp: LocalDateTime,

        public val stacktrace: String,
    ) : BallastDebuggerEventV3() {
        override fun toString(): String {
            return "Uncaught error\n$stacktrace"
        }
    }

    @Serializable
    public enum class StatusV3 {
        NotStarted, Running, ShuttingDown, Cleared
    }

}
