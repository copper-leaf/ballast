package com.copperleaf.ballast.debugger.versions.v3

import com.copperleaf.ballast.debugger.versions.ClientModelConverter
import com.copperleaf.ballast.debugger.versions.v2.BallastDebuggerActionV2
import com.copperleaf.ballast.debugger.versions.v2.BallastDebuggerEventV2

public class ClientModelConverterV2ToV3 : ClientModelConverter<
        BallastDebuggerEventV2,
        BallastDebuggerEventV3,
        BallastDebuggerActionV2,
        BallastDebuggerActionV3
        > {

    override fun mapEvent(event: BallastDebuggerEventV2): BallastDebuggerEventV3 = with(event) {
        return when (this) {
            is BallastDebuggerEventV2.Heartbeat -> BallastDebuggerEventV3.Heartbeat(
                connectionId = connectionId,
                connectionBallastVersion = connectionBallastVersion,
            )

            is BallastDebuggerEventV2.RefreshViewModelStart -> BallastDebuggerEventV3.RefreshViewModelStart(
                connectionId = connectionId,
                viewModelName = viewModelName,
            )

            is BallastDebuggerEventV2.RefreshViewModelComplete -> BallastDebuggerEventV3.RefreshViewModelComplete(
                connectionId = connectionId,
                viewModelName = viewModelName,
            )

            is BallastDebuggerEventV2.ViewModelStarted -> BallastDebuggerEventV3.ViewModelStatusChanged(
                connectionId = connectionId,
                viewModelName = viewModelName,
                viewModelType = viewModelType,
                uuid = uuid,
                timestamp = timestamp,
                status = BallastDebuggerEventV3.StatusV3.Running,
            )

            is BallastDebuggerEventV2.ViewModelCleared -> BallastDebuggerEventV3.ViewModelStatusChanged(
                connectionId = connectionId,
                viewModelName = viewModelName,
                viewModelType = "", // uh oh, versions < 3 don't include this value
                uuid = uuid,
                timestamp = timestamp,
                status = BallastDebuggerEventV3.StatusV3.Cleared,
            )

            is BallastDebuggerEventV2.InputQueued -> BallastDebuggerEventV3.InputQueued(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                inputType = inputType,
                serializedInput = inputToStringValue,
                inputContentType = "text/*"
            )

            is BallastDebuggerEventV2.InputAccepted -> BallastDebuggerEventV3.InputAccepted(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                inputType = inputType,
                serializedInput = inputToStringValue,
                inputContentType = "text/*"
            )

            is BallastDebuggerEventV2.InputRejected -> BallastDebuggerEventV3.InputRejected(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                inputType = inputType,
                serializedInput = inputToStringValue,
                inputContentType = "text/*"
            )

            is BallastDebuggerEventV2.InputDropped -> BallastDebuggerEventV3.InputDropped(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                inputType = inputType,
                serializedInput = inputToStringValue,
                inputContentType = "text/*"
            )

            is BallastDebuggerEventV2.InputHandledSuccessfully -> BallastDebuggerEventV3.InputHandledSuccessfully(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                inputType = inputType,
                serializedInput = inputToStringValue,
                inputContentType = "text/*"
            )

            is BallastDebuggerEventV2.InputCancelled -> BallastDebuggerEventV3.InputCancelled(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                inputType = inputType,
                serializedInput = inputToStringValue,
                inputContentType = "text/*"
            )

            is BallastDebuggerEventV2.InputHandlerError -> BallastDebuggerEventV3.InputHandlerError(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                inputType = inputType,
                serializedInput = inputToStringValue,
                inputContentType = "text/*",
                stacktrace = stacktrace,
            )

            is BallastDebuggerEventV2.EventQueued -> BallastDebuggerEventV3.EventQueued(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                eventType = eventType,
                serializedEvent = eventToStringValue,
                eventContentType = "text/*",
            )

            is BallastDebuggerEventV2.EventEmitted -> BallastDebuggerEventV3.EventEmitted(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                eventType = eventType,
                serializedEvent = eventToStringValue,
                eventContentType = "text/*",
            )

            is BallastDebuggerEventV2.EventHandledSuccessfully -> BallastDebuggerEventV3.EventHandledSuccessfully(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                eventType = eventType,
                serializedEvent = eventToStringValue,
                eventContentType = "text/*",
            )

            is BallastDebuggerEventV2.EventHandlerError -> BallastDebuggerEventV3.EventHandlerError(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                eventType = eventType,
                serializedEvent = eventToStringValue,
                eventContentType = "text/*",
                stacktrace = stacktrace,
            )

            is BallastDebuggerEventV2.EventProcessingStarted -> BallastDebuggerEventV3.EventProcessingStarted(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
            )

            is BallastDebuggerEventV2.EventProcessingStopped -> BallastDebuggerEventV3.EventProcessingStopped(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
            )

            is BallastDebuggerEventV2.StateChanged -> BallastDebuggerEventV3.StateChanged(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                stateType = stateType,
                serializedState = stateToStringValue,
                stateContentType = "text/*",
            )

            is BallastDebuggerEventV2.SideJobQueued -> BallastDebuggerEventV3.SideJobQueued(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                key = key,
            )

            is BallastDebuggerEventV2.SideJobStarted -> BallastDebuggerEventV3.SideJobStarted(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                key = key,
                restartState = restartState,
            )

            is BallastDebuggerEventV2.SideJobCompleted -> BallastDebuggerEventV3.SideJobCompleted(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                key = key,
                restartState = restartState,
            )

            is BallastDebuggerEventV2.SideJobCancelled -> BallastDebuggerEventV3.SideJobCancelled(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                key = key,
                restartState = restartState,
            )

            is BallastDebuggerEventV2.SideJobError -> BallastDebuggerEventV3.SideJobError(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                key = key,
                restartState = restartState,
                stacktrace = stacktrace,
            )

            is BallastDebuggerEventV2.UnhandledError -> BallastDebuggerEventV3.UnhandledError(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                stacktrace = stacktrace,
            )
        }
    }

    override fun mapAction(action: BallastDebuggerActionV3): BallastDebuggerActionV2 = with(action) {
        return when (this) {
            is BallastDebuggerActionV3.RequestViewModelRefresh -> BallastDebuggerActionV2.RequestViewModelRefresh(
                connectionId = connectionId,
                viewModelName = viewModelName,
            )

            is BallastDebuggerActionV3.RequestRestoreState -> BallastDebuggerActionV2.RequestRestoreState(
                connectionId = connectionId,
                viewModelName = viewModelName,
                stateUuid = stateUuid,
            )

            is BallastDebuggerActionV3.RequestResendInput -> BallastDebuggerActionV2.RequestResendInput(
                connectionId = connectionId,
                viewModelName = viewModelName,
                inputUuid = inputUuid,
            )
        }
    }
}
