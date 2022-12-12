package com.copperleaf.ballast.debugger.server.v1

import com.copperleaf.ballast.debugger.models.BallastDebuggerAction
import com.copperleaf.ballast.debugger.models.BallastDebuggerEvent
import com.copperleaf.ballast.debugger.server.ClientModelMapper
import kotlinx.serialization.json.Json

class ClientModelMapperV1 : ClientModelMapper {
    private val debuggerEventJson: Json = Json {
        isLenient = true
    }

    override val supported: Boolean = true

// Incoming
// ---------------------------------------------------------------------------------------------------------------------

    override fun mapIncoming(incoming: String): BallastDebuggerEvent {
        return debuggerEventJson
            .decodeFromString(BallastDebuggerEventV1.serializer(), incoming)
            .toLatestVersionEvent()
    }

    private fun BallastDebuggerEventV1.toLatestVersionEvent(): BallastDebuggerEvent {
        return when(this) {
            is BallastDebuggerEventV1.Heartbeat -> BallastDebuggerEvent.Heartbeat(
                connectionId = connectionId,
                connectionBallastVersion = connectionBallastVersion,
            )
            is BallastDebuggerEventV1.RefreshViewModelStart -> BallastDebuggerEvent.RefreshViewModelStart(
                connectionId = connectionId,
                viewModelName = viewModelName,
            )
            is BallastDebuggerEventV1.RefreshViewModelComplete -> BallastDebuggerEvent.RefreshViewModelComplete(
                connectionId = connectionId,
                viewModelName = viewModelName,
            )
            is BallastDebuggerEventV1.ViewModelStarted -> BallastDebuggerEvent.ViewModelStarted(
                connectionId = connectionId,
                viewModelName = viewModelName,
                viewModelType = viewModelType,
                uuid = uuid,
                timestamp = timestamp,
            )
            is BallastDebuggerEventV1.ViewModelCleared -> BallastDebuggerEvent.ViewModelCleared(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
            )
            is BallastDebuggerEventV1.InputQueued -> BallastDebuggerEvent.InputQueued(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                inputType = inputType,
                inputToStringValue = inputToStringValue,
            )
            is BallastDebuggerEventV1.InputAccepted -> BallastDebuggerEvent.InputAccepted(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                inputType = inputType,
                inputToStringValue = inputToStringValue,
            )
            is BallastDebuggerEventV1.InputRejected -> BallastDebuggerEvent.InputRejected(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                inputType = inputType,
                inputToStringValue = inputToStringValue,
            )
            is BallastDebuggerEventV1.InputDropped -> BallastDebuggerEvent.InputDropped(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                inputType = inputType,
                inputToStringValue = inputToStringValue,
            )
            is BallastDebuggerEventV1.InputHandledSuccessfully -> BallastDebuggerEvent.InputHandledSuccessfully(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                inputType = inputType,
                inputToStringValue = inputToStringValue,
            )
            is BallastDebuggerEventV1.InputCancelled -> BallastDebuggerEvent.InputCancelled(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                inputType = inputType,
                inputToStringValue = inputToStringValue,
            )
            is BallastDebuggerEventV1.InputHandlerError -> BallastDebuggerEvent.InputHandlerError(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                inputType = inputType,
                inputToStringValue = inputToStringValue,
                stacktrace = stacktrace,
            )
            is BallastDebuggerEventV1.EventQueued -> BallastDebuggerEvent.EventQueued(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                eventType = eventType,
                eventToStringValue = eventToStringValue,
            )
            is BallastDebuggerEventV1.EventEmitted -> BallastDebuggerEvent.EventEmitted(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                eventType = eventType,
                eventToStringValue = eventToStringValue,
            )
            is BallastDebuggerEventV1.EventHandledSuccessfully -> BallastDebuggerEvent.EventHandledSuccessfully(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                eventType = eventType,
                eventToStringValue = eventToStringValue,
            )
            is BallastDebuggerEventV1.EventHandlerError -> BallastDebuggerEvent.EventHandlerError(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                eventType = eventType,
                eventToStringValue = eventToStringValue,
                stacktrace = stacktrace,
            )
            is BallastDebuggerEventV1.EventProcessingStarted -> BallastDebuggerEvent.EventProcessingStarted(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
            )
            is BallastDebuggerEventV1.EventProcessingStopped -> BallastDebuggerEvent.EventProcessingStopped(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
            )
            is BallastDebuggerEventV1.StateChanged -> BallastDebuggerEvent.StateChanged(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                stateType = stateType,
                stateToStringValue = stateToStringValue,
            )
            is BallastDebuggerEventV1.SideJobQueued -> BallastDebuggerEvent.SideJobQueued(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                key = key,
            )
            is BallastDebuggerEventV1.SideJobStarted -> BallastDebuggerEvent.SideJobStarted(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                key = key,
                restartState = restartState,
            )
            is BallastDebuggerEventV1.SideJobCompleted -> BallastDebuggerEvent.SideJobCompleted(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                key = key,
                restartState = restartState,
            )
            is BallastDebuggerEventV1.SideJobCancelled -> BallastDebuggerEvent.SideJobCancelled(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                key = key,
                restartState = restartState,
            )
            is BallastDebuggerEventV1.SideJobError -> BallastDebuggerEvent.SideJobError(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                key = key,
                restartState = restartState,
                stacktrace = stacktrace,
            )
            is BallastDebuggerEventV1.UnhandledError -> BallastDebuggerEvent.UnhandledError(
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
            .encodeToString(BallastDebuggerActionV1.serializer(), outgoing.toV1Action())
    }

    private fun BallastDebuggerAction.toV1Action(): BallastDebuggerActionV1 {
        return when(this) {
            is BallastDebuggerAction.RequestViewModelRefresh -> BallastDebuggerActionV1.RequestViewModelRefresh(
                connectionId = connectionId,
                viewModelName = viewModelName,
            )
            is BallastDebuggerAction.RequestRestoreState -> BallastDebuggerActionV1.RequestRestoreState(
                connectionId = connectionId,
                viewModelName = viewModelName,
                stateUuid = stateUuid,
            )
            is BallastDebuggerAction.RequestResendInput -> BallastDebuggerActionV1.RequestResendInput(
                 connectionId = connectionId,
                 viewModelName = viewModelName,
                 inputUuid = inputUuid,
            )
        }
    }
}
