package com.copperleaf.ballast.debugger.models

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

public fun LocalDateTime.Companion.now(): LocalDateTime {
    return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
}

@ExperimentalTime
public operator fun LocalDateTime.minus(other: LocalDateTime): Duration {
    return this.toInstant(TimeZone.currentSystemDefault()) - other.toInstant(TimeZone.currentSystemDefault())
}

public data class BallastApplicationState(
    public val connections: List<BallastConnectionState> = emptyList(),
)

public data class BallastConnectionState(
    public val connectionId: String,
    public val connectionBallastVersion: String = "",
    public val viewModels: List<BallastViewModelState> = emptyList(),
    public val firstSeen: LocalDateTime = LocalDateTime.now(),
    public val lastSeen: LocalDateTime = LocalDateTime.now(),
)

public data class BallastViewModelState(
    public val connectionId: String,
    public val viewModelName: String,

    public val inputs: List<String> = emptyList(),
    public val events: List<String> = emptyList(),
    public val sideEffects: List<String> = emptyList(),
    public val states: List<String> = emptyList(),
    public val latestState: String = "",

    public val viewModelActive: Boolean = false,
    public val eventProcessingActive: Boolean = false,
    public val inputInProgress: Boolean = false,
    public val sideEffectsInProgress: Boolean = false,

    public val firstSeen: LocalDateTime = LocalDateTime.now(),
    public val lastSeen: LocalDateTime = LocalDateTime.now(),
    public val fullHistory: List<BallastDebuggerEvent> = emptyList(),
    public val refreshing: Boolean = false,
)

public fun BallastApplicationState.updateInConnection(
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
                    this.add(0, BallastConnectionState(connectionId).block())
                }
            }
            .toList(),
    )
}

public fun BallastConnectionState.updateInViewModel(
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
                        this.add(0, BallastViewModelState(connectionId, viewModelName).block())
                    }
                }
            }
            .toList()
    )
}

public fun BallastViewModelState.updateWithDebuggerEvent(event: BallastDebuggerEvent): BallastViewModelState {
    val updatedState = when (event) {
        is BallastDebuggerEvent.RefreshViewModelStart -> {
            BallastViewModelState(connectionId, viewModelName, refreshing = true)
        }
        is BallastDebuggerEvent.RefreshViewModelComplete -> {
            copy(refreshing = false)
        }

        is BallastDebuggerEvent.ViewModelStarted -> {
            copy(viewModelActive = true)
        }
        is BallastDebuggerEvent.ViewModelCleared -> {
            copy(viewModelActive = false)
        }

        is BallastDebuggerEvent.InputAccepted -> {
            copy(
                inputs = listOf(event.inputType) + inputs,
                viewModelActive = true,
                inputInProgress = true
            )
        }
        is BallastDebuggerEvent.InputHandledSuccessfully -> {
            copy(
                inputInProgress = false,
            )
        }
        is BallastDebuggerEvent.InputCancelled -> {
            copy(
                inputInProgress = false,
            )
        }
        is BallastDebuggerEvent.InputHandlerError -> {
            copy(
                inputInProgress = false,
            )
        }

        is BallastDebuggerEvent.EventEmitted -> {
            copy(
                events = listOf(event.eventType) + events,
                viewModelActive = true,
            )
        }

        is BallastDebuggerEvent.EventProcessingStarted -> {
            copy(
                eventProcessingActive = true,
            )
        }
        is BallastDebuggerEvent.EventProcessingStopped -> {
            copy(
                eventProcessingActive = false,
            )
        }

        is BallastDebuggerEvent.StateChanged -> {
            copy(
                states = listOf(event.state) + states,
                latestState = event.state,
                viewModelActive = true,
            )
        }

        is BallastDebuggerEvent.SideEffectStarted -> {
            copy(
                sideEffects = listOf(event.key) + sideEffects,
                viewModelActive = true,
                sideEffectsInProgress = true,
            )
        }
        is BallastDebuggerEvent.SideEffectCompleted -> {
            copy(
                sideEffectsInProgress = false,
            )
        }
        is BallastDebuggerEvent.SideEffectError -> {
            copy(
                sideEffectsInProgress = false,
            )
        }
        else -> this
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
        lastSeen = LocalDateTime.now(),
        fullHistory = newHistory
    )
}
