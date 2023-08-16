package com.copperleaf.ballast.debugger.versions.v4

import com.copperleaf.ballast.debugger.versions.ClientModelConverter
import com.copperleaf.ballast.debugger.versions.v3.BallastDebuggerActionV3
import com.copperleaf.ballast.debugger.versions.v3.BallastDebuggerEventV3

public class ClientModelConverterV3ToV4 : ClientModelConverter<
        BallastDebuggerEventV3,
        BallastDebuggerEventV4,
        BallastDebuggerActionV3,
        BallastDebuggerActionV4
        > {

    override fun mapEvent(event: BallastDebuggerEventV3): BallastDebuggerEventV4 = with(event) {
        return when (this) {
            is BallastDebuggerEventV3.Heartbeat -> BallastDebuggerEventV4.Heartbeat(
                connectionId = connectionId,
                connectionBallastVersion = connectionBallastVersion,
            )

            is BallastDebuggerEventV3.RefreshViewModelStart -> BallastDebuggerEventV4.RefreshViewModelStart(
                connectionId = connectionId,
                viewModelName = viewModelName,
            )

            is BallastDebuggerEventV3.RefreshViewModelComplete -> BallastDebuggerEventV4.RefreshViewModelComplete(
                connectionId = connectionId,
                viewModelName = viewModelName,
            )

            is BallastDebuggerEventV3.ViewModelStatusChanged -> BallastDebuggerEventV4.ViewModelStatusChanged(
                connectionId = connectionId,
                viewModelName = viewModelName,
                viewModelType = viewModelType,
                uuid = uuid,
                timestamp = timestamp,
                status = when (status) {
                    BallastDebuggerEventV3.StatusV3.NotStarted -> BallastDebuggerEventV4.StatusV4.NotStarted
                    BallastDebuggerEventV3.StatusV3.Running -> BallastDebuggerEventV4.StatusV4.Running
                    BallastDebuggerEventV3.StatusV3.ShuttingDown -> BallastDebuggerEventV4.StatusV4.ShuttingDown
                    BallastDebuggerEventV3.StatusV3.Cleared -> BallastDebuggerEventV4.StatusV4.Cleared
                },
            )

            is BallastDebuggerEventV3.InputQueued -> BallastDebuggerEventV4.InputQueued(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                inputType = inputType,
                serializedInput = serializedInput,
                inputContentType = inputContentType,
            )

            is BallastDebuggerEventV3.InputAccepted -> BallastDebuggerEventV4.InputAccepted(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                inputType = inputType,
                serializedInput = serializedInput,
                inputContentType = inputContentType,
            )

            is BallastDebuggerEventV3.InputRejected -> BallastDebuggerEventV4.InputRejected(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                inputType = inputType,
                serializedInput = serializedInput,
                inputContentType = inputContentType,
            )

            is BallastDebuggerEventV3.InputDropped -> BallastDebuggerEventV4.InputDropped(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                inputType = inputType,
                serializedInput = serializedInput,
                inputContentType = inputContentType,
            )

            is BallastDebuggerEventV3.InputHandledSuccessfully -> BallastDebuggerEventV4.InputHandledSuccessfully(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                inputType = inputType,
                serializedInput = serializedInput,
                inputContentType = inputContentType,
            )

            is BallastDebuggerEventV3.InputCancelled -> BallastDebuggerEventV4.InputCancelled(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                inputType = inputType,
                serializedInput = serializedInput,
                inputContentType = inputContentType,
            )

            is BallastDebuggerEventV3.InputHandlerError -> BallastDebuggerEventV4.InputHandlerError(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                inputType = inputType,
                serializedInput = serializedInput,
                inputContentType = inputContentType,
                stacktrace = stacktrace,
            )

            is BallastDebuggerEventV3.EventQueued -> BallastDebuggerEventV4.EventQueued(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                eventType = eventType,
                serializedEvent = serializedEvent,
                eventContentType = eventContentType,
            )

            is BallastDebuggerEventV3.EventEmitted -> BallastDebuggerEventV4.EventEmitted(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                eventType = eventType,
                serializedEvent = serializedEvent,
                eventContentType = eventContentType,
            )

            is BallastDebuggerEventV3.EventHandledSuccessfully -> BallastDebuggerEventV4.EventHandledSuccessfully(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                eventType = eventType,
                serializedEvent = serializedEvent,
                eventContentType = eventContentType,
            )

            is BallastDebuggerEventV3.EventHandlerError -> BallastDebuggerEventV4.EventHandlerError(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                eventType = eventType,
                serializedEvent = serializedEvent,
                eventContentType = eventContentType,
                stacktrace = stacktrace,
            )

            is BallastDebuggerEventV3.EventProcessingStarted -> BallastDebuggerEventV4.EventProcessingStarted(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
            )

            is BallastDebuggerEventV3.EventProcessingStopped -> BallastDebuggerEventV4.EventProcessingStopped(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
            )

            is BallastDebuggerEventV3.StateChanged -> BallastDebuggerEventV4.StateChanged(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                stateType = stateType,
                serializedState = serializedState,
                stateContentType = stateContentType,
            )

            is BallastDebuggerEventV3.SideJobQueued -> BallastDebuggerEventV4.SideJobQueued(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                key = key,
            )

            is BallastDebuggerEventV3.SideJobStarted -> BallastDebuggerEventV4.SideJobStarted(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                key = key,
                restartState = restartState,
            )

            is BallastDebuggerEventV3.SideJobCompleted -> BallastDebuggerEventV4.SideJobCompleted(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                key = key,
                restartState = restartState,
            )

            is BallastDebuggerEventV3.SideJobCancelled -> BallastDebuggerEventV4.SideJobCancelled(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                key = key,
                restartState = restartState,
            )

            is BallastDebuggerEventV3.SideJobError -> BallastDebuggerEventV4.SideJobError(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                key = key,
                restartState = restartState,
                stacktrace = stacktrace,
            )

            is BallastDebuggerEventV3.UnhandledError -> BallastDebuggerEventV4.UnhandledError(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                stacktrace = stacktrace,
            )

            is BallastDebuggerEventV3.InterceptorAttached -> BallastDebuggerEventV4.InterceptorAttached(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                interceptorType = interceptorType,
                interceptorToStringValue = interceptorToStringValue,
            )
            is BallastDebuggerEventV3.InterceptorFailed -> BallastDebuggerEventV4.InterceptorFailed(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                interceptorType = interceptorType,
                interceptorToStringValue = interceptorToStringValue,
                stacktrace = stacktrace,
            )
        }
    }

    override fun mapAction(action: BallastDebuggerActionV4): BallastDebuggerActionV3 = with(action) {
        return when (this) {
            is BallastDebuggerActionV4.RequestViewModelRefresh -> BallastDebuggerActionV3.RequestViewModelRefresh(
                connectionId = connectionId,
                viewModelName = viewModelName,
            )

            is BallastDebuggerActionV4.RequestRestoreState -> BallastDebuggerActionV3.RequestRestoreState(
                connectionId = connectionId,
                viewModelName = viewModelName,
                stateUuid = stateUuid,
            )

            is BallastDebuggerActionV4.RequestResendInput -> BallastDebuggerActionV3.RequestResendInput(
                connectionId = connectionId,
                viewModelName = viewModelName,
                inputUuid = inputUuid,
            )

            is BallastDebuggerActionV4.RequestReplaceState -> {
                error("RequestReplaceState only supported on clients v4+")
            }
        }
    }
}
