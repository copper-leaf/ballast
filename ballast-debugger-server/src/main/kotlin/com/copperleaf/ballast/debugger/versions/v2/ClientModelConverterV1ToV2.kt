package com.copperleaf.ballast.debugger.versions.v2

import com.copperleaf.ballast.debugger.versions.ClientModelConverter
import com.copperleaf.ballast.debugger.versions.v1.BallastDebuggerActionV1
import com.copperleaf.ballast.debugger.versions.v1.BallastDebuggerEventV1

class ClientModelConverterV1ToV2 : ClientModelConverter<
        BallastDebuggerEventV1,
        BallastDebuggerEventV2,
        BallastDebuggerActionV1,
        BallastDebuggerActionV2
        > {

    override fun mapEvent(event: BallastDebuggerEventV1): BallastDebuggerEventV2 = with(event) {
        return when (this) {
            is BallastDebuggerEventV1.Heartbeat -> BallastDebuggerEventV2.Heartbeat(
                connectionId = connectionId,
                connectionBallastVersion = connectionBallastVersion,
            )

            is BallastDebuggerEventV1.RefreshViewModelStart -> BallastDebuggerEventV2.RefreshViewModelStart(
                connectionId = connectionId,
                viewModelName = viewModelName,
            )

            is BallastDebuggerEventV1.RefreshViewModelComplete -> BallastDebuggerEventV2.RefreshViewModelComplete(
                connectionId = connectionId,
                viewModelName = viewModelName,
            )

            is BallastDebuggerEventV1.ViewModelStarted -> BallastDebuggerEventV2.ViewModelStarted(
                connectionId = connectionId,
                viewModelName = viewModelName,
                viewModelType = viewModelType,
                uuid = uuid,
                timestamp = timestamp,
            )

            is BallastDebuggerEventV1.ViewModelCleared -> BallastDebuggerEventV2.ViewModelCleared(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
            )

            is BallastDebuggerEventV1.InputQueued -> BallastDebuggerEventV2.InputQueued(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                inputType = inputType,
                inputToStringValue = inputToStringValue,
            )

            is BallastDebuggerEventV1.InputAccepted -> BallastDebuggerEventV2.InputAccepted(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                inputType = inputType,
                inputToStringValue = inputToStringValue,
            )

            is BallastDebuggerEventV1.InputRejected -> BallastDebuggerEventV2.InputRejected(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                inputType = inputType,
                inputToStringValue = inputToStringValue,
            )

            is BallastDebuggerEventV1.InputDropped -> BallastDebuggerEventV2.InputDropped(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                inputType = inputType,
                inputToStringValue = inputToStringValue,
            )

            is BallastDebuggerEventV1.InputHandledSuccessfully -> BallastDebuggerEventV2.InputHandledSuccessfully(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                inputType = inputType,
                inputToStringValue = inputToStringValue,
            )

            is BallastDebuggerEventV1.InputCancelled -> BallastDebuggerEventV2.InputCancelled(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                inputType = inputType,
                inputToStringValue = inputToStringValue,
            )

            is BallastDebuggerEventV1.InputHandlerError -> BallastDebuggerEventV2.InputHandlerError(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                inputType = inputType,
                inputToStringValue = inputToStringValue,
                stacktrace = stacktrace,
            )

            is BallastDebuggerEventV1.EventQueued -> BallastDebuggerEventV2.EventQueued(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                eventType = eventType,
                eventToStringValue = eventToStringValue,
            )

            is BallastDebuggerEventV1.EventEmitted -> BallastDebuggerEventV2.EventEmitted(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                eventType = eventType,
                eventToStringValue = eventToStringValue,
            )

            is BallastDebuggerEventV1.EventHandledSuccessfully -> BallastDebuggerEventV2.EventHandledSuccessfully(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                eventType = eventType,
                eventToStringValue = eventToStringValue,
            )

            is BallastDebuggerEventV1.EventHandlerError -> BallastDebuggerEventV2.EventHandlerError(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                eventType = eventType,
                eventToStringValue = eventToStringValue,
                stacktrace = stacktrace,
            )

            is BallastDebuggerEventV1.EventProcessingStarted -> BallastDebuggerEventV2.EventProcessingStarted(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
            )

            is BallastDebuggerEventV1.EventProcessingStopped -> BallastDebuggerEventV2.EventProcessingStopped(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
            )

            is BallastDebuggerEventV1.StateChanged -> BallastDebuggerEventV2.StateChanged(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                stateType = stateType,
                stateToStringValue = stateToStringValue,
            )

            is BallastDebuggerEventV1.SideJobQueued -> BallastDebuggerEventV2.SideJobQueued(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                key = key,
            )

            is BallastDebuggerEventV1.SideJobStarted -> BallastDebuggerEventV2.SideJobStarted(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                key = key,
                restartState = restartState,
            )

            is BallastDebuggerEventV1.SideJobCompleted -> BallastDebuggerEventV2.SideJobCompleted(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                key = key,
                restartState = restartState,
            )

            is BallastDebuggerEventV1.SideJobCancelled -> BallastDebuggerEventV2.SideJobCancelled(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                key = key,
                restartState = restartState,
            )

            is BallastDebuggerEventV1.SideJobError -> BallastDebuggerEventV2.SideJobError(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                key = key,
                restartState = restartState,
                stacktrace = stacktrace,
            )

            is BallastDebuggerEventV1.UnhandledError -> BallastDebuggerEventV2.UnhandledError(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                stacktrace = stacktrace,
            )
        }
    }

    override fun mapAction(action: BallastDebuggerActionV2): BallastDebuggerActionV1 = with(action) {
        return when (this) {
            is BallastDebuggerActionV2.RequestViewModelRefresh -> BallastDebuggerActionV1.RequestViewModelRefresh(
                connectionId = connectionId,
                viewModelName = viewModelName,
            )

            is BallastDebuggerActionV2.RequestRestoreState -> BallastDebuggerActionV1.RequestRestoreState(
                connectionId = connectionId,
                viewModelName = viewModelName,
                stateUuid = stateUuid,
            )

            is BallastDebuggerActionV2.RequestResendInput -> BallastDebuggerActionV1.RequestResendInput(
                connectionId = connectionId,
                viewModelName = viewModelName,
                inputUuid = inputUuid,
            )
        }
    }
}
