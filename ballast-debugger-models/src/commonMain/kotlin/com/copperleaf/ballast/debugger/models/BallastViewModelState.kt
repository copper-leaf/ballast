package com.copperleaf.ballast.debugger.models

import com.copperleaf.ballast.debugger.utils.minus
import com.copperleaf.ballast.debugger.utils.now
import com.copperleaf.ballast.debugger.utils.removeFraction
import com.copperleaf.ballast.debugger.versions.v4.BallastDebuggerEventV4
import kotlinx.datetime.LocalDateTime
import kotlin.time.DurationUnit

public data class BallastViewModelState(
    public val connectionId: String,
    public val viewModelName: String,

    public val viewModelType: String = "",

    public val inputs: List<BallastInputState> = emptyList(),
    public val events: List<BallastEventState> = emptyList(),
    public val sideJobs: List<BallastSideJobState> = emptyList(),
    public val states: List<BallastStateSnapshot> = emptyList(),
    public val interceptors: List<BallastInterceptorState> = emptyList(),

    public val viewModelActive: Boolean = false,
    public val eventProcessingActive: Boolean = false,

    public val firstSeen: LocalDateTime = LocalDateTime.now(),
    public val lastSeen: LocalDateTime = LocalDateTime.now(),
    public val fullHistory: List<BallastDebuggerEventV4> = emptyList(),
    public val refreshing: Boolean = false,
) {
    public val runningInputCount: Int = inputs.count { it.status == BallastInputState.Status.Running }
    public val runningEventCount: Int = events.count { it.status == BallastEventState.Status.Running }
    public val runningSideJobCount: Int = sideJobs.count { it.status == BallastSideJobState.Status.Running }

    public val inputInProgress: Boolean = runningInputCount > 0
    public val eventInProgress: Boolean = runningEventCount > 0
    public val sideJobsInProgress: Boolean = runningSideJobCount > 0

    private fun updateInput(
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

    private fun updateEvent(
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

    private fun appendStateSnapshot(
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

    private fun updateSideJob(
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

    private fun updateInterceptor(
        uuid: String,
        block: BallastInterceptorState.() -> BallastInterceptorState,
    ): BallastViewModelState {
        val indexOfInterceptor = interceptors.indexOfFirst { it.uuid == uuid }

        return this.copy(
            interceptors = interceptors
                .toMutableList()
                .apply {
                    if (indexOfInterceptor != -1) {
                        // we're updating a value in an existing connection
                        this[indexOfInterceptor] = this[indexOfInterceptor].block().copy(lastSeen = LocalDateTime.now())
                    } else {
                        // this is the first time we're seeing this connection, create a new entry for it
                        this.add(0, BallastInterceptorState(connectionId, viewModelName, uuid).block())
                    }
                }
                .toList()
        )
    }

    public fun updateWithDebuggerEvent(
        event: BallastDebuggerEventV4,
        actualValue: Any?
    ): BallastViewModelState {
        val updatedState = when (event) {
            is BallastDebuggerEventV4.RefreshViewModelStart -> {
                BallastViewModelState(
                    connectionId = connectionId,
                    viewModelName = viewModelName,
                    viewModelType = viewModelType,
                    refreshing = true,
                    firstSeen = event.timestamp,
                    lastSeen = event.timestamp,
                )
            }

            is BallastDebuggerEventV4.RefreshViewModelComplete -> {
                copy(refreshing = false)
            }

            is BallastDebuggerEventV4.ViewModelStatusChanged -> {
                copy(
                    viewModelActive = event.status != BallastDebuggerEventV4.StatusV4.Cleared,
                    viewModelType = event.viewModelType
                )
            }

            is BallastDebuggerEventV4.InputQueued -> {
                updateInput(event.uuid, actualValue) {
                    copy(
                        type = event.inputType,
                        serializedValue = event.serializedInput,
                        contentType = event.inputContentType,
                        status = BallastInputState.Status.Queued,
                        firstSeen = event.timestamp,
                    )
                }
            }

            is BallastDebuggerEventV4.InputAccepted -> {
                updateInput(event.uuid, actualValue) {
                    copy(
                        type = event.inputType,
                        serializedValue = event.serializedInput,
                        contentType = event.inputContentType,
                        status = BallastInputState.Status.Running,
                        lastSeen = event.timestamp,
                    )
                }
            }

            is BallastDebuggerEventV4.InputHandledSuccessfully -> {
                updateInput(event.uuid, actualValue) {
                    copy(
                        type = event.inputType,
                        serializedValue = event.serializedInput,
                        contentType = event.inputContentType,
                        lastSeen = event.timestamp,
                        status = BallastInputState.Status.Completed(
                            duration = (event.timestamp - this.firstSeen).removeFraction(DurationUnit.MICROSECONDS),
                        ),
                    )
                }
            }

            is BallastDebuggerEventV4.InputCancelled -> {
                updateInput(event.uuid, actualValue) {
                    copy(
                        type = event.inputType,
                        serializedValue = event.serializedInput,
                        contentType = event.inputContentType,
                        lastSeen = event.timestamp,
                        status = BallastInputState.Status.Cancelled(
                            duration = (event.timestamp - this.firstSeen).removeFraction(DurationUnit.MICROSECONDS),
                        ),
                    )
                }
            }

            is BallastDebuggerEventV4.InputHandlerError -> {
                updateInput(event.uuid, actualValue) {
                    copy(
                        type = event.inputType,
                        serializedValue = event.serializedInput,
                        contentType = event.inputContentType,
                        lastSeen = event.timestamp,
                        status = BallastInputState.Status.Error(
                            duration = (event.timestamp - this.firstSeen).removeFraction(DurationUnit.MICROSECONDS),
                            stacktrace = event.stacktrace,
                        ),
                    )
                }
            }

            is BallastDebuggerEventV4.InputDropped -> {
                updateInput(event.uuid, actualValue) {
                    copy(
                        type = event.inputType,
                        serializedValue = event.serializedInput,
                        contentType = event.inputContentType,
                        status = BallastInputState.Status.Dropped,
                        lastSeen = event.timestamp,
                    )
                }
            }

            is BallastDebuggerEventV4.InputRejected -> {
                updateInput(event.uuid, actualValue) {
                    copy(
                        type = event.inputType,
                        serializedValue = event.serializedInput,
                        contentType = event.inputContentType,
                        status = BallastInputState.Status.Rejected,
                        lastSeen = event.timestamp,
                    )
                }
            }

            is BallastDebuggerEventV4.EventQueued -> {
                updateEvent(event.uuid) {
                    copy(
                        type = event.eventType,
                        serializedValue = event.serializedEvent,
                        contentType = event.eventContentType,
                        status = BallastEventState.Status.Queued,
                        firstSeen = event.timestamp,
                    )
                }
            }

            is BallastDebuggerEventV4.EventEmitted -> {
                updateEvent(event.uuid) {
                    copy(
                        type = event.eventType,
                        serializedValue = event.serializedEvent,
                        contentType = event.eventContentType,
                        status = BallastEventState.Status.Running,
                        lastSeen = event.timestamp,
                    )
                }
            }

            is BallastDebuggerEventV4.EventHandledSuccessfully -> {
                updateEvent(event.uuid) {
                    copy(
                        type = event.eventType,
                        serializedValue = event.serializedEvent,
                        contentType = event.eventContentType,
                        lastSeen = event.timestamp,
                        status = BallastEventState.Status.Completed(
                            duration = (event.timestamp - this.firstSeen).removeFraction(DurationUnit.MICROSECONDS),
                        ),
                    )
                }
            }

            is BallastDebuggerEventV4.EventHandlerError -> {
                updateEvent(event.uuid) {
                    copy(
                        type = event.eventType,
                        serializedValue = event.serializedEvent,
                        contentType = event.eventContentType,
                        lastSeen = event.timestamp,
                        status = BallastEventState.Status.Error(
                            duration = (event.timestamp - this.firstSeen).removeFraction(DurationUnit.MICROSECONDS),
                            stacktrace = event.stacktrace,
                        ),
                    )
                }
            }

            is BallastDebuggerEventV4.EventProcessingStarted -> {
                copy(eventProcessingActive = true)
            }

            is BallastDebuggerEventV4.EventProcessingStopped -> {
                copy(eventProcessingActive = false)
            }

            is BallastDebuggerEventV4.StateChanged -> {
                appendStateSnapshot(event.uuid, actualValue) {
                    copy(
                        emittedAt = event.timestamp,
                        type = event.stateType,
                        serializedValue = event.serializedState,
                        contentType = event.stateContentType,
                    )
                }
            }

            is BallastDebuggerEventV4.SideJobQueued -> {
                updateSideJob(event.uuid) {
                    copy(
                        key = event.key,
                        status = BallastSideJobState.Status.Queued,
                        firstSeen = event.timestamp,
                    )
                }
            }

            is BallastDebuggerEventV4.SideJobStarted -> {
                updateSideJob(event.uuid) {
                    copy(
                        key = event.key,
                        restartState = event.restartState,
                        status = BallastSideJobState.Status.Running,
                        lastSeen = event.timestamp,
                    )
                }
            }

            is BallastDebuggerEventV4.SideJobCompleted -> {
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

            is BallastDebuggerEventV4.SideJobCancelled -> {
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

            is BallastDebuggerEventV4.SideJobError -> {
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

            is BallastDebuggerEventV4.InterceptorAttached -> {
                updateInterceptor(event.uuid) {
                    copy(
                        type = event.interceptorType,
                        toStringValue = event.interceptorToStringValue,
                        status = BallastInterceptorState.Status.Attached,
                        lastSeen = event.timestamp,
                    )
                }
            }
            is BallastDebuggerEventV4.InterceptorFailed -> {
                updateInterceptor(event.uuid) {
                    copy(
                        type = event.interceptorType,
                        toStringValue = event.interceptorToStringValue,
                        status = BallastInterceptorState.Status.Attached,
                        lastSeen = event.timestamp,
                    )
                }
            }

            is BallastDebuggerEventV4.Heartbeat -> { this }
            is BallastDebuggerEventV4.UnhandledError -> { this }
        }

        val newHistory = when (event) {
            is BallastDebuggerEventV4.Heartbeat,
            is BallastDebuggerEventV4.RefreshViewModelComplete,
            is BallastDebuggerEventV4.RefreshViewModelStart -> {
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

}
