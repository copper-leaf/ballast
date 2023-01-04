package com.copperleaf.ballast.debugger.models

import com.copperleaf.ballast.SideJobScope
import com.copperleaf.ballast.debugger.utils.minus
import com.copperleaf.ballast.debugger.utils.now
import com.copperleaf.ballast.debugger.utils.removeFraction
import kotlinx.datetime.LocalDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

// Application
// ---------------------------------------------------------------------------------------------------------------------

public data class BallastApplicationState(
    public val connections: List<BallastConnectionState> = emptyList(),
)

// Connection
// ---------------------------------------------------------------------------------------------------------------------

public data class BallastConnectionState(
    public val connectionId: String,
    public val connectionBallastVersion: String = "",
    public val viewModels: List<BallastViewModelState> = emptyList(),
    public val firstSeen: LocalDateTime = LocalDateTime.now(),
    public val lastSeen: LocalDateTime = LocalDateTime.now(),
) {
    public fun isActive(currentTime: LocalDateTime): Boolean {
        return (currentTime - lastSeen) <= 10.seconds
    }
}

public fun BallastApplicationState.updateConnection(
    connectionId: String,
    block: BallastConnectionState.() -> BallastConnectionState,
): BallastApplicationState {
    val indexOfConnection = connections.indexOfFirst { it.connectionId == connectionId }

    return this.copy(
        connections = connections
            .toMutableList()
            .apply {
                if (indexOfConnection != -1) {
                    // we're updating a value in an existing connection
                    this[indexOfConnection] = this[indexOfConnection].block().copy(lastSeen = LocalDateTime.now())
                } else {
                    // this is the first time we're seeing this connection, create a new entry for it
                    this.add(0, BallastConnectionState(connectionId, firstSeen  = LocalDateTime.now()).block())
                }
            }
            .toList(),
    )
}

public fun BallastApplicationState.removeConnection(
    connectionId: String,
): BallastApplicationState {
    val indexOfConnection = connections.indexOfFirst { it.connectionId == connectionId }

    return this.copy(
        connections = connections
            .toMutableList()
            .apply {
                if (indexOfConnection != -1) {
                    removeAt(indexOfConnection)
                }
            }
            .toList(),
    )
}

// ViewModel
// ---------------------------------------------------------------------------------------------------------------------

public data class BallastViewModelState(
    public val connectionId: String,
    public val viewModelName: String,

    public val viewModelType: String = "",

    public val inputs: List<BallastInputState> = emptyList(),
    public val events: List<BallastEventState> = emptyList(),
    public val sideJobs: List<BallastSideJobState> = emptyList(),
    public val states: List<BallastStateSnapshot> = emptyList(),

    public val viewModelActive: Boolean = false,
    public val eventProcessingActive: Boolean = false,

    public val firstSeen: LocalDateTime = LocalDateTime.now(),
    public val lastSeen: LocalDateTime = LocalDateTime.now(),
    public val fullHistory: List<BallastDebuggerEvent> = emptyList(),
    public val refreshing: Boolean = false,
) {
    public val runningInputCount: Int = inputs.count { it.status == BallastInputState.Status.Running }
    public val runningEventCount: Int = events.count { it.status == BallastEventState.Status.Running }
    public val runningSideJobCount: Int = sideJobs.count { it.status == BallastSideJobState.Status.Running }

    public val inputInProgress: Boolean = runningInputCount > 0
    public val eventInProgress: Boolean = runningEventCount > 0
    public val sideJobsInProgress: Boolean = runningSideJobCount > 0
}

public fun BallastConnectionState.updateViewModel(
    viewModelName: String?,
    block: BallastViewModelState.() -> BallastViewModelState,
): BallastConnectionState {
    val indexOfViewModel = viewModels.indexOfFirst { it.viewModelName == viewModelName }

    return this.copy(
        viewModels = viewModels
            .toMutableList()
            .apply {
                if (viewModelName != null) {
                    if (indexOfViewModel != -1) {
                        // we're updating a value in an existing connection
                        this[indexOfViewModel] = this[indexOfViewModel].block().copy(lastSeen = LocalDateTime.now())
                    } else {
                        // this is the first time we're seeing this connection, create a new entry for it
                        this.add(
                            0,
                            BallastViewModelState(connectionId, viewModelName, firstSeen = LocalDateTime.now()).block()
                        )
                    }
                }
            }
            .toList()
    )
}

// Inputs
// ---------------------------------------------------------------------------------------------------------------------

public data class BallastInputState(
    public val connectionId: String,
    public val viewModelName: String,
    public val uuid: String,
    public val actualInput: Any?,

    public val type: String = "",
    public val toStringValue: String = "",

    public val status: Status = Status.Queued,

    public val firstSeen: LocalDateTime = LocalDateTime.now(),
    public val lastSeen: LocalDateTime = LocalDateTime.now(),
) {
    public sealed class Status {
        public object Queued : Status() {
            override fun toString(): String = "Queued"
        }

        public object Dropped : Status() {
            override fun toString(): String = "Dropped"
        }

        public object Running : Status() {
            override fun toString(): String = "Running"
        }

        public object Rejected : Status() {
            override fun toString(): String = "Rejected"
        }

        public data class Cancelled(val duration: Duration) : Status() {
            override fun toString(): String = "Cancelled after $duration"
        }

        public data class Error(val duration: Duration, val stacktrace: String) : Status() {
            override fun toString(): String = "Failed after $duration"
        }

        public data class Completed(val duration: Duration) : Status() {
            override fun toString(): String = "Completed after $duration"
        }
    }
}

public fun BallastViewModelState.updateInput(
    uuid: String,
    actualInput: Any?,
    block: BallastInputState.() -> BallastInputState,
): BallastViewModelState {
    val indexOfInput = inputs.indexOfFirst { it.uuid == uuid }

    return this.copy(
        inputs = inputs
            .toMutableList()
            .apply {
                if (indexOfInput != -1) {
                    // we're updating a value in an existing connection
                    this[indexOfInput] = this[indexOfInput].block()
                } else {
                    // this is the first time we're seeing this connection, create a new entry for it
                    this.add(
                        0, BallastInputState(
                            connectionId = connectionId,
                            viewModelName = viewModelName,
                            uuid = uuid,
                            actualInput = actualInput,
                        ).block()
                    )
                }
            }
            .toList()
    )
}

// Events
// ---------------------------------------------------------------------------------------------------------------------

public data class BallastEventState(
    public val connectionId: String,
    public val viewModelName: String,
    public val uuid: String,

    public val type: String = "",
    public val toStringValue: String = "",

    public val status: Status = Status.Queued,

    public val firstSeen: LocalDateTime = LocalDateTime.now(),
    public val lastSeen: LocalDateTime = LocalDateTime.now(),
) {
    public sealed class Status {
        public object Queued : Status() {
            override fun toString(): String = "Queued"
        }

        public object Running : Status() {
            override fun toString(): String = "Running"
        }

        public data class Error(val duration: Duration, val stacktrace: String) : Status() {
            override fun toString(): String = "Failed after $duration"
        }

        public data class Completed(val duration: Duration) : Status() {
            override fun toString(): String = "Completed after $duration"
        }
    }
}

public fun BallastViewModelState.updateEvent(
    uuid: String,
    block: BallastEventState.() -> BallastEventState,
): BallastViewModelState {
    val indexOfEvents = events.indexOfFirst { it.uuid == uuid }

    return this.copy(
        events = events
            .toMutableList()
            .apply {
                if (indexOfEvents != -1) {
                    // we're updating a value in an existing connection
                    this[indexOfEvents] = this[indexOfEvents].block().copy(lastSeen = LocalDateTime.now())
                } else {
                    // this is the first time we're seeing this connection, create a new entry for it
                    this.add(0, BallastEventState(connectionId, viewModelName, uuid).block())
                }
            }
            .toList()
    )
}

// States
// ---------------------------------------------------------------------------------------------------------------------

public data class BallastStateSnapshot(
    public val connectionId: String,
    public val viewModelName: String,
    public val uuid: String,
    public val actualState: Any?,

    public val type: String = "",
    public val toStringValue: String = "",

    public val emittedAt: LocalDateTime = LocalDateTime.now(),
)

public fun BallastViewModelState.appendStateSnapshot(
    uuid: String,
    actualState: Any?,
    block: BallastStateSnapshot.() -> BallastStateSnapshot,
): BallastViewModelState {
    val state = BallastStateSnapshot(
        connectionId = connectionId,
        viewModelName = viewModelName,
        uuid = uuid,
        actualState = actualState,
    ).block()

    return this.copy(
        states = listOf(state) + states
    )
}

// SideJobs
// ---------------------------------------------------------------------------------------------------------------------

public data class BallastSideJobState(
    public val connectionId: String,
    public val viewModelName: String,
    public val uuid: String,

    public val key: String = "",
    public val restartState: SideJobScope.RestartState = SideJobScope.RestartState.Initial,
    public val status: Status = Status.Running,

    public val firstSeen: LocalDateTime = LocalDateTime.now(),
    public val lastSeen: LocalDateTime = LocalDateTime.now(),
) {
    public sealed class Status {
        public object Queued : Status() {
            override fun toString(): String = "Queued"
        }

        public object Running : Status() {
            override fun toString(): String = "Running"
        }

        public data class Cancelled(val duration: Duration) : Status() {
            override fun toString(): String = "Cancelled after $duration"
        }

        public data class Error(val duration: Duration, val stacktrace: String) : Status() {
            override fun toString(): String = "Failed after $duration"
        }

        public data class Completed(val duration: Duration) : Status() {
            override fun toString(): String = "Completed after $duration"
        }
    }
}

public fun BallastViewModelState.updateSideJob(
    uuid: String,
    block: BallastSideJobState.() -> BallastSideJobState,
): BallastViewModelState {
    val indexOfSideJob = sideJobs.indexOfFirst { it.uuid == uuid }

    return this.copy(
        sideJobs = sideJobs
            .toMutableList()
            .apply {
                if (indexOfSideJob != -1) {
                    // we're updating a value in an existing connection
                    this[indexOfSideJob] = this[indexOfSideJob].block().copy(lastSeen = LocalDateTime.now())
                } else {
                    // this is the first time we're seeing this connection, create a new entry for it
                    this.add(0, BallastSideJobState(connectionId, viewModelName, uuid).block())
                }
            }
            .toList()
    )
}

// Process Debugger Event
// ---------------------------------------------------------------------------------------------------------------------

public fun BallastViewModelState.updateWithDebuggerEvent(
    event: BallastDebuggerEvent,
    actualValue: Any?
): BallastViewModelState {
    val updatedState = when (event) {
        is BallastDebuggerEvent.RefreshViewModelStart -> {
            BallastViewModelState(
                connectionId = connectionId,
                viewModelName = viewModelName,
                viewModelType = viewModelType,
                refreshing = true,
                firstSeen = event.timestamp,
                lastSeen = event.timestamp,
            )
        }

        is BallastDebuggerEvent.RefreshViewModelComplete -> {
            copy(refreshing = false)
        }

        is BallastDebuggerEvent.ViewModelStarted -> {
            copy(viewModelActive = true, viewModelType = event.viewModelType)
        }

        is BallastDebuggerEvent.ViewModelCleared -> {
            copy(viewModelActive = false)
        }

        is BallastDebuggerEvent.InputQueued -> {
            updateInput(event.uuid, actualValue) {
                copy(
                    type = event.inputType,
                    toStringValue = event.inputToStringValue,
                    status = BallastInputState.Status.Queued,
                    firstSeen = event.timestamp,
                )
            }
        }

        is BallastDebuggerEvent.InputAccepted -> {
            updateInput(event.uuid, actualValue) {
                copy(
                    type = event.inputType,
                    toStringValue = event.inputToStringValue,
                    status = BallastInputState.Status.Running,
                    lastSeen = event.timestamp,
                )
            }
        }

        is BallastDebuggerEvent.InputHandledSuccessfully -> {
            updateInput(event.uuid, actualValue) {
                copy(
                    type = event.inputType,
                    toStringValue = event.inputToStringValue,
                    lastSeen = event.timestamp,
                    status = BallastInputState.Status.Completed(
                        duration = (event.timestamp - this.firstSeen).removeFraction(DurationUnit.MICROSECONDS),
                    ),
                )
            }
        }

        is BallastDebuggerEvent.InputCancelled -> {
            updateInput(event.uuid, actualValue) {
                copy(
                    type = event.inputType,
                    toStringValue = event.inputToStringValue,
                    lastSeen = event.timestamp,
                    status = BallastInputState.Status.Cancelled(
                        duration = (event.timestamp - this.firstSeen).removeFraction(DurationUnit.MICROSECONDS),
                    ),
                )
            }
        }

        is BallastDebuggerEvent.InputHandlerError -> {
            updateInput(event.uuid, actualValue) {
                copy(
                    type = event.inputType,
                    toStringValue = event.inputToStringValue,
                    lastSeen = event.timestamp,
                    status = BallastInputState.Status.Error(
                        duration = (event.timestamp - this.firstSeen).removeFraction(DurationUnit.MICROSECONDS),
                        stacktrace = event.stacktrace,
                    ),
                )
            }
        }

        is BallastDebuggerEvent.InputDropped -> {
            updateInput(event.uuid, actualValue) {
                copy(
                    type = event.inputType,
                    toStringValue = event.inputToStringValue,
                    status = BallastInputState.Status.Dropped,
                    lastSeen = event.timestamp,
                )
            }
        }

        is BallastDebuggerEvent.InputRejected -> {
            updateInput(event.uuid, actualValue) {
                copy(
                    type = event.inputType,
                    toStringValue = event.inputToStringValue,
                    status = BallastInputState.Status.Rejected,
                    lastSeen = event.timestamp,
                )
            }
        }

        is BallastDebuggerEvent.EventQueued -> {
            updateEvent(event.uuid) {
                copy(
                    type = event.eventType,
                    toStringValue = event.eventToStringValue,
                    status = BallastEventState.Status.Queued,
                    firstSeen = event.timestamp,
                )
            }
        }

        is BallastDebuggerEvent.EventEmitted -> {
            updateEvent(event.uuid) {
                copy(
                    type = event.eventType,
                    toStringValue = event.eventToStringValue,
                    status = BallastEventState.Status.Running,
                    lastSeen = event.timestamp,
                )
            }
        }

        is BallastDebuggerEvent.EventHandledSuccessfully -> {
            updateEvent(event.uuid) {
                copy(
                    type = event.eventType,
                    toStringValue = event.eventToStringValue,
                    lastSeen = event.timestamp,
                    status = BallastEventState.Status.Completed(
                        duration = (event.timestamp - this.firstSeen).removeFraction(DurationUnit.MICROSECONDS),
                    ),
                )
            }
        }

        is BallastDebuggerEvent.EventHandlerError -> {
            updateEvent(event.uuid) {
                copy(
                    type = event.eventType,
                    toStringValue = event.eventToStringValue,
                    lastSeen = event.timestamp,
                    status = BallastEventState.Status.Error(
                        duration = (event.timestamp - this.firstSeen).removeFraction(DurationUnit.MICROSECONDS),
                        stacktrace = event.stacktrace,
                    ),
                )
            }
        }

        is BallastDebuggerEvent.EventProcessingStarted -> {
            copy(eventProcessingActive = true)
        }

        is BallastDebuggerEvent.EventProcessingStopped -> {
            copy(eventProcessingActive = false)
        }

        is BallastDebuggerEvent.StateChanged -> {
            appendStateSnapshot(event.uuid, actualValue) {
                copy(
                    emittedAt = event.timestamp,
                    type = event.stateType,
                    toStringValue = event.stateToStringValue,
                )
            }
        }

        is BallastDebuggerEvent.SideJobQueued -> {
            updateSideJob(event.uuid) {
                copy(
                    key = event.key,
                    status = BallastSideJobState.Status.Queued,
                    firstSeen = event.timestamp,
                )
            }
        }

        is BallastDebuggerEvent.SideJobStarted -> {
            updateSideJob(event.uuid) {
                copy(
                    key = event.key,
                    restartState = event.restartState,
                    status = BallastSideJobState.Status.Running,
                    lastSeen = event.timestamp,
                )
            }
        }

        is BallastDebuggerEvent.SideJobCompleted -> {
            updateSideJob(event.uuid) {
                copy(
                    key = event.key,
                    restartState = event.restartState,
                    lastSeen = event.timestamp,
                    status = BallastSideJobState.Status.Completed(
                        duration = (event.timestamp - this.firstSeen).removeFraction(DurationUnit.MICROSECONDS),
                    ),
                )
            }
        }

        is BallastDebuggerEvent.SideJobCancelled -> {
            updateSideJob(event.uuid) {
                copy(
                    key = event.key,
                    restartState = event.restartState,
                    lastSeen = event.timestamp,
                    status = BallastSideJobState.Status.Cancelled(
                        duration = (event.timestamp - this.firstSeen).removeFraction(DurationUnit.MICROSECONDS),
                    ),
                )
            }
        }

        is BallastDebuggerEvent.SideJobError -> {
            updateSideJob(event.uuid) {
                copy(
                    key = event.key,
                    restartState = event.restartState,
                    lastSeen = event.timestamp,
                    status = BallastSideJobState.Status.Error(
                        duration = (event.timestamp - this.firstSeen).removeFraction(DurationUnit.MICROSECONDS),
                        stacktrace = event.stacktrace
                    ),
                )
            }
        }

        else -> {
            this
        }
    }

    val newHistory = when (event) {
        is BallastDebuggerEvent.Heartbeat,
        is BallastDebuggerEvent.RefreshViewModelComplete,
        is BallastDebuggerEvent.RefreshViewModelStart -> {
            fullHistory
        }

        else -> {
            listOf(event) + fullHistory
        }
    }

    return updatedState.copy(
        lastSeen = event.timestamp,
        fullHistory = newHistory
    )
}
