package com.copperleaf.ballast.debugger.versions.v2

import com.copperleaf.ballast.SideJobScope
import com.copperleaf.ballast.debugger.utils.now
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

private const val EVENT_MODEL_BASE_CLASS_NAME = "com.copperleaf.ballast.debugger.models.BallastDebuggerEvent"

@Serializable
public sealed class BallastDebuggerEventV2 {
    public abstract val connectionId: String
    public abstract val viewModelName: String?
    public abstract val uuid: String?
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
    ) : BallastDebuggerEventV2() {
        override val viewModelName: String? = null
        override val uuid: String? = null
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
    ) : BallastDebuggerEventV2() {
        override val uuid: String? = null
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
    ) : BallastDebuggerEventV2() {
        override val uuid: String? = null
        override val timestamp: LocalDateTime = LocalDateTime.now()
    }

// ViewModel
// ---------------------------------------------------------------------------------------------------------------------

    @Serializable
    @SerialName("$EVENT_MODEL_BASE_CLASS_NAME.ViewModelStarted")
    public class ViewModelStarted(
        override val connectionId: String,
        override val viewModelName: String,
        public val viewModelType: String,
        override val uuid: String,
        override val timestamp: LocalDateTime,
    ) : BallastDebuggerEventV2()

    @Serializable
    @SerialName("$EVENT_MODEL_BASE_CLASS_NAME.ViewModelCleared")
    public class ViewModelCleared(
        override val connectionId: String,
        override val viewModelName: String,
        override val uuid: String,
        override val timestamp: LocalDateTime,
    ) : BallastDebuggerEventV2()

// Inputs
// ---------------------------------------------------------------------------------------------------------------------

    @Serializable
    @SerialName("$EVENT_MODEL_BASE_CLASS_NAME.InputQueued")
    public class InputQueued(
        override val connectionId: String,
        override val viewModelName: String,
        override val uuid: String,
        override val timestamp: LocalDateTime,

        public val inputType: String,
        public val inputToStringValue: String,
    ) : BallastDebuggerEventV2()

    @Serializable
    @SerialName("$EVENT_MODEL_BASE_CLASS_NAME.InputAccepted")
    public class InputAccepted(
        override val connectionId: String,
        override val viewModelName: String,
        override val uuid: String,
        override val timestamp: LocalDateTime,

        public val inputType: String,
        public val inputToStringValue: String,
    ) : BallastDebuggerEventV2()

    @Serializable
    @SerialName("$EVENT_MODEL_BASE_CLASS_NAME.InputRejected")
    public class InputRejected(
        override val connectionId: String,
        override val viewModelName: String,
        override val uuid: String,
        override val timestamp: LocalDateTime,

        public val inputType: String,
        public val inputToStringValue: String,
    ) : BallastDebuggerEventV2()

    @Serializable
    @SerialName("$EVENT_MODEL_BASE_CLASS_NAME.InputDropped")
    public class InputDropped(
        override val connectionId: String,
        override val viewModelName: String,
        override val uuid: String,
        override val timestamp: LocalDateTime,

        public val inputType: String,
        public val inputToStringValue: String,
    ) : BallastDebuggerEventV2()

    @Serializable
    @SerialName("$EVENT_MODEL_BASE_CLASS_NAME.InputHandledSuccessfully")
    public class InputHandledSuccessfully(
        override val connectionId: String,
        override val viewModelName: String,
        override val uuid: String,
        override val timestamp: LocalDateTime,

        public val inputType: String,
        public val inputToStringValue: String,
    ) : BallastDebuggerEventV2()

    @Serializable
    @SerialName("$EVENT_MODEL_BASE_CLASS_NAME.InputCancelled")
    public class InputCancelled(
        override val connectionId: String,
        override val viewModelName: String,
        override val uuid: String,
        override val timestamp: LocalDateTime,

        public val inputType: String,
        public val inputToStringValue: String,
    ) : BallastDebuggerEventV2()

    @Serializable
    @SerialName("$EVENT_MODEL_BASE_CLASS_NAME.InputHandlerError")
    public class InputHandlerError(
        override val connectionId: String,
        override val viewModelName: String,
        override val uuid: String,
        override val timestamp: LocalDateTime,

        public val inputType: String,
        public val inputToStringValue: String,
        public val stacktrace: String,
    ) : BallastDebuggerEventV2()

// Events
// ---------------------------------------------------------------------------------------------------------------------

    @Serializable
    @SerialName("$EVENT_MODEL_BASE_CLASS_NAME.EventQueued")
    public class EventQueued(
        override val connectionId: String,
        override val viewModelName: String,
        override val uuid: String,
        override val timestamp: LocalDateTime,

        public val eventType: String,
        public val eventToStringValue: String,
    ) : BallastDebuggerEventV2()

    @Serializable
    @SerialName("$EVENT_MODEL_BASE_CLASS_NAME.EventEmitted")
    public class EventEmitted(
        override val connectionId: String,
        override val viewModelName: String,
        override val uuid: String,
        override val timestamp: LocalDateTime,

        public val eventType: String,
        public val eventToStringValue: String,
    ) : BallastDebuggerEventV2()

    @Serializable
    @SerialName("$EVENT_MODEL_BASE_CLASS_NAME.EventHandledSuccessfully")
    public class EventHandledSuccessfully(
        override val connectionId: String,
        override val viewModelName: String,
        override val uuid: String,
        override val timestamp: LocalDateTime,

        public val eventType: String,
        public val eventToStringValue: String,
    ) : BallastDebuggerEventV2()

    @Serializable
    @SerialName("$EVENT_MODEL_BASE_CLASS_NAME.EventHandlerError")
    public class EventHandlerError(
        override val connectionId: String,
        override val viewModelName: String,
        override val uuid: String,
        override val timestamp: LocalDateTime,

        public val eventType: String,
        public val eventToStringValue: String,
        public val stacktrace: String,
    ) : BallastDebuggerEventV2()

    @Serializable
    @SerialName("$EVENT_MODEL_BASE_CLASS_NAME.EventProcessingStarted")
    public class EventProcessingStarted(
        override val connectionId: String,
        override val viewModelName: String,
        override val uuid: String,
        override val timestamp: LocalDateTime,
    ) : BallastDebuggerEventV2()

    @Serializable
    @SerialName("$EVENT_MODEL_BASE_CLASS_NAME.EventProcessingStopped")
    public class EventProcessingStopped(
        override val connectionId: String,
        override val viewModelName: String,
        override val uuid: String,
        override val timestamp: LocalDateTime,
    ) : BallastDebuggerEventV2()

// States
// ---------------------------------------------------------------------------------------------------------------------

    @Serializable
    @SerialName("$EVENT_MODEL_BASE_CLASS_NAME.StateChanged")
    public class StateChanged(
        override val connectionId: String,
        override val viewModelName: String,
        override val uuid: String,
        override val timestamp: LocalDateTime,

        public val stateType: String,
        public val stateToStringValue: String,
    ) : BallastDebuggerEventV2()

// Side-jobs
// ---------------------------------------------------------------------------------------------------------------------

    @Serializable
    @SerialName("$EVENT_MODEL_BASE_CLASS_NAME.SideJobQueued")
    public class SideJobQueued(
        override val connectionId: String,
        override val viewModelName: String,
        override val uuid: String,
        override val timestamp: LocalDateTime,

        public val key: String,
    ) : BallastDebuggerEventV2()

    @Serializable
    @SerialName("$EVENT_MODEL_BASE_CLASS_NAME.SideJobStarted")
    public class SideJobStarted(
        override val connectionId: String,
        override val viewModelName: String,
        override val uuid: String,
        override val timestamp: LocalDateTime,

        public val key: String,
        public val restartState: SideJobScope.RestartState,
    ) : BallastDebuggerEventV2()

    @Serializable
    @SerialName("$EVENT_MODEL_BASE_CLASS_NAME.SideJobCompleted")
    public class SideJobCompleted(
        override val connectionId: String,
        override val viewModelName: String,
        override val uuid: String,
        override val timestamp: LocalDateTime,

        public val key: String,
        public val restartState: SideJobScope.RestartState,
    ) : BallastDebuggerEventV2()

    @Serializable
    @SerialName("$EVENT_MODEL_BASE_CLASS_NAME.SideJobCancelled")
    public class SideJobCancelled(
        override val connectionId: String,
        override val viewModelName: String,
        override val uuid: String,
        override val timestamp: LocalDateTime,

        public val key: String,
        public val restartState: SideJobScope.RestartState,
    ) : BallastDebuggerEventV2()

    @Serializable
    @SerialName("$EVENT_MODEL_BASE_CLASS_NAME.SideJobError")
    public class SideJobError(
        override val connectionId: String,
        override val viewModelName: String,
        override val uuid: String,
        override val timestamp: LocalDateTime,

        public val key: String,
        public val restartState: SideJobScope.RestartState,
        public val stacktrace: String,
    ) : BallastDebuggerEventV2()

// Other
// ---------------------------------------------------------------------------------------------------------------------

    @Serializable
    @SerialName("$EVENT_MODEL_BASE_CLASS_NAME.UnhandledError")
    public class UnhandledError(
        override val connectionId: String,
        override val viewModelName: String,
        override val uuid: String,
        override val timestamp: LocalDateTime,

        public val stacktrace: String,
    ) : BallastDebuggerEventV2()
}
