package com.copperleaf.ballast.debugger.server.versions.v2

import com.copperleaf.ballast.debugger.models.BallastDebuggerAction
import com.copperleaf.ballast.debugger.models.BallastDebuggerEvent
import com.copperleaf.ballast.debugger.server.ClientModelMapper
import kotlinx.serialization.json.Json

class ClientModelMapperV2 : ClientModelMapper {
    private val debuggerEventJson: Json = Json {
        isLenient = true
    }

    override val supported: Boolean = true

// Incoming
// ---------------------------------------------------------------------------------------------------------------------

    override fun mapIncoming(incoming: String): BallastDebuggerEvent {
        return debuggerEventJson
            .decodeFromString(BallastDebuggerEventV2.serializer(), incoming)
            .toLatestVersionEvent()
    }

    private fun BallastDebuggerEventV2.toLatestVersionEvent(): BallastDebuggerEvent {
        return when(this) {
            is BallastDebuggerEventV2.Heartbeat -> BallastDebuggerEvent.Heartbeat(
                connectionId = connectionId,
                connectionBallastVersion = connectionBallastVersion,
            )
            is BallastDebuggerEventV2.RefreshViewModelStart -> BallastDebuggerEvent.RefreshViewModelStart(
                connectionId = connectionId,
                viewModelName = viewModelName,
            )
            is BallastDebuggerEventV2.RefreshViewModelComplete -> BallastDebuggerEvent.RefreshViewModelComplete(
                connectionId = connectionId,
                viewModelName = viewModelName,
            )
            is BallastDebuggerEventV2.ViewModelStarted -> BallastDebuggerEvent.ViewModelStarted(
                connectionId = connectionId,
                viewModelName = viewModelName,
                viewModelType = viewModelType,
                uuid = uuid,
                timestamp = timestamp,
            )
            is BallastDebuggerEventV2.ViewModelCleared -> BallastDebuggerEvent.ViewModelCleared(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
            )
            is BallastDebuggerEventV2.InputQueued -> BallastDebuggerEvent.InputQueued(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                inputType = inputType,
                inputToStringValue = inputToStringValue,
            )
            is BallastDebuggerEventV2.InputAccepted -> BallastDebuggerEvent.InputAccepted(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                inputType = inputType,
                inputToStringValue = inputToStringValue,
            )
            is BallastDebuggerEventV2.InputRejected -> BallastDebuggerEvent.InputRejected(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                inputType = inputType,
                inputToStringValue = inputToStringValue,
            )
            is BallastDebuggerEventV2.InputDropped -> BallastDebuggerEvent.InputDropped(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                inputType = inputType,
                inputToStringValue = inputToStringValue,
            )
            is BallastDebuggerEventV2.InputHandledSuccessfully -> BallastDebuggerEvent.InputHandledSuccessfully(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                inputType = inputType,
                inputToStringValue = inputToStringValue,
            )
            is BallastDebuggerEventV2.InputCancelled -> BallastDebuggerEvent.InputCancelled(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                inputType = inputType,
                inputToStringValue = inputToStringValue,
            )
            is BallastDebuggerEventV2.InputHandlerError -> BallastDebuggerEvent.InputHandlerError(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                inputType = inputType,
                inputToStringValue = inputToStringValue,
                stacktrace = stacktrace,
            )
            is BallastDebuggerEventV2.EventQueued -> BallastDebuggerEvent.EventQueued(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                eventType = eventType,
                eventToStringValue = eventToStringValue,
            )
            is BallastDebuggerEventV2.EventEmitted -> BallastDebuggerEvent.EventEmitted(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                eventType = eventType,
                eventToStringValue = eventToStringValue,
            )
            is BallastDebuggerEventV2.EventHandledSuccessfully -> BallastDebuggerEvent.EventHandledSuccessfully(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                eventType = eventType,
                eventToStringValue = eventToStringValue,
            )
            is BallastDebuggerEventV2.EventHandlerError -> BallastDebuggerEvent.EventHandlerError(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                eventType = eventType,
                eventToStringValue = eventToStringValue,
                stacktrace = stacktrace,
            )
            is BallastDebuggerEventV2.EventProcessingStarted -> BallastDebuggerEvent.EventProcessingStarted(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
            )
            is BallastDebuggerEventV2.EventProcessingStopped -> BallastDebuggerEvent.EventProcessingStopped(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
            )
            is BallastDebuggerEventV2.StateChanged -> BallastDebuggerEvent.StateChanged(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                stateType = stateType,
                stateToStringValue = stateToStringValue,
            )
            is BallastDebuggerEventV2.SideJobQueued -> BallastDebuggerEvent.SideJobQueued(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                key = key,
            )
            is BallastDebuggerEventV2.SideJobStarted -> BallastDebuggerEvent.SideJobStarted(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                key = key,
                restartState = restartState,
            )
            is BallastDebuggerEventV2.SideJobCompleted -> BallastDebuggerEvent.SideJobCompleted(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                key = key,
                restartState = restartState,
            )
            is BallastDebuggerEventV2.SideJobCancelled -> BallastDebuggerEvent.SideJobCancelled(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                key = key,
                restartState = restartState,
            )
            is BallastDebuggerEventV2.SideJobError -> BallastDebuggerEvent.SideJobError(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                key = key,
                restartState = restartState,
                stacktrace = stacktrace,
            )
            is BallastDebuggerEventV2.UnhandledError -> BallastDebuggerEvent.UnhandledError(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                stacktrace = stacktrace,
            )
        }
    }

// Outgoing
// ---------------------------------------------------------------------------------------------------------------------

    override fun mapOutgoing(outgoing: BallastDebuggerAction): String {
        return debuggerEventJson
            .encodeToString(BallastDebuggerActionV2.serializer(), outgoing.toV2Action())
    }

    private fun BallastDebuggerAction.toV2Action(): BallastDebuggerActionV2 {
        return when(this) {
            is BallastDebuggerAction.RequestViewModelRefresh -> BallastDebuggerActionV2.RequestViewModelRefresh(
                connectionId = connectionId,
                viewModelName = viewModelName,
            )
            is BallastDebuggerAction.RequestRestoreState -> BallastDebuggerActionV2.RequestRestoreState(
                connectionId = connectionId,
                viewModelName = viewModelName,
                stateUuid = stateUuid,
            )
            is BallastDebuggerAction.RequestResendInput -> BallastDebuggerActionV2.RequestResendInput(
                connectionId = connectionId,
                viewModelName = viewModelName,
                inputUuid = inputUuid,
            )
        }
    }
}
