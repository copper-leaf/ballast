package com.copperleaf.ballast.debugger.versions.v5

import com.copperleaf.ballast.debugger.versions.ClientModelConverter
import com.copperleaf.ballast.debugger.versions.v4.BallastDebuggerActionV4
import com.copperleaf.ballast.debugger.versions.v4.BallastDebuggerEventV4

public class ClientModelConverterV4ToV5 : ClientModelConverter<
        BallastDebuggerEventV4,
        BallastDebuggerEventV5,
        BallastDebuggerActionV4,
        BallastDebuggerActionV5
        > {

    override fun mapEvent(event: BallastDebuggerEventV4): BallastDebuggerEventV5 = with(event) {
        return when (this) {
            is BallastDebuggerEventV4.Heartbeat -> BallastDebuggerEventV5.Heartbeat(
                connectionId = connectionId,
                connectionBallastVersion = connectionBallastVersion,
            )

            is BallastDebuggerEventV4.RefreshViewModelStart -> BallastDebuggerEventV5.RefreshViewModelStart(
                connectionId = connectionId,
                viewModelName = viewModelName,
            )

            is BallastDebuggerEventV4.RefreshViewModelComplete -> BallastDebuggerEventV5.RefreshViewModelComplete(
                connectionId = connectionId,
                viewModelName = viewModelName,
            )

            is BallastDebuggerEventV4.ViewModelStatusChanged -> BallastDebuggerEventV5.ViewModelStatusChanged(
                connectionId = connectionId,
                viewModelName = viewModelName,
                viewModelType = viewModelType,
                uuid = uuid,
                timestamp = timestamp,
                status = when (status) {
                    BallastDebuggerEventV4.StatusV4.NotStarted -> BallastDebuggerEventV5.StatusV5.NotStarted
                    BallastDebuggerEventV4.StatusV4.Running -> BallastDebuggerEventV5.StatusV5.Running
                    BallastDebuggerEventV4.StatusV4.ShuttingDown -> BallastDebuggerEventV5.StatusV5.ShuttingDown
                    BallastDebuggerEventV4.StatusV4.Cleared -> BallastDebuggerEventV5.StatusV5.Cleared
                },
            )

            is BallastDebuggerEventV4.InputQueued -> BallastDebuggerEventV5.InputQueued(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                inputType = inputType,
                serializedInput = serializedInput,
                inputContentType = inputContentType,
            )

            is BallastDebuggerEventV4.InputAccepted -> BallastDebuggerEventV5.InputAccepted(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                inputType = inputType,
                serializedInput = serializedInput,
                inputContentType = inputContentType,
            )

            is BallastDebuggerEventV4.InputRejected -> BallastDebuggerEventV5.InputRejected(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                inputType = inputType,
                serializedInput = serializedInput,
                inputContentType = inputContentType,
            )

            is BallastDebuggerEventV4.InputDropped -> BallastDebuggerEventV5.InputDropped(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                inputType = inputType,
                serializedInput = serializedInput,
                inputContentType = inputContentType,
            )

            is BallastDebuggerEventV4.InputHandledSuccessfully -> BallastDebuggerEventV5.InputHandledSuccessfully(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                inputType = inputType,
                serializedInput = serializedInput,
                inputContentType = inputContentType,
            )

            is BallastDebuggerEventV4.InputCancelled -> BallastDebuggerEventV5.InputCancelled(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                inputType = inputType,
                serializedInput = serializedInput,
                inputContentType = inputContentType,
            )

            is BallastDebuggerEventV4.InputHandlerError -> BallastDebuggerEventV5.InputHandlerError(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                inputType = inputType,
                serializedInput = serializedInput,
                inputContentType = inputContentType,
                stacktrace = stacktrace,
            )

            is BallastDebuggerEventV4.EventQueued -> BallastDebuggerEventV5.EventQueued(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                eventType = eventType,
                serializedEvent = serializedEvent,
                eventContentType = eventContentType,
            )

            is BallastDebuggerEventV4.EventEmitted -> BallastDebuggerEventV5.EventEmitted(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                eventType = eventType,
                serializedEvent = serializedEvent,
                eventContentType = eventContentType,
            )

            is BallastDebuggerEventV4.EventHandledSuccessfully -> BallastDebuggerEventV5.EventHandledSuccessfully(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                eventType = eventType,
                serializedEvent = serializedEvent,
                eventContentType = eventContentType,
            )

            is BallastDebuggerEventV4.EventHandlerError -> BallastDebuggerEventV5.EventHandlerError(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                eventType = eventType,
                serializedEvent = serializedEvent,
                eventContentType = eventContentType,
                stacktrace = stacktrace,
            )

            is BallastDebuggerEventV4.EventProcessingStarted -> BallastDebuggerEventV5.EventProcessingStarted(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
            )

            is BallastDebuggerEventV4.EventProcessingStopped -> BallastDebuggerEventV5.EventProcessingStopped(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
            )

            is BallastDebuggerEventV4.StateChanged -> BallastDebuggerEventV5.StateChanged(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                stateType = stateType,
                serializedState = serializedState,
                stateContentType = stateContentType,
            )

            is BallastDebuggerEventV4.SideJobQueued -> BallastDebuggerEventV5.SideJobQueued(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                key = key,
            )

            is BallastDebuggerEventV4.SideJobStarted -> BallastDebuggerEventV5.SideJobStarted(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                key = key,
                restartState = restartState,
            )

            is BallastDebuggerEventV4.SideJobCompleted -> BallastDebuggerEventV5.SideJobCompleted(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                key = key,
                restartState = restartState,
            )

            is BallastDebuggerEventV4.SideJobCancelled -> BallastDebuggerEventV5.SideJobCancelled(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                key = key,
                restartState = restartState,
            )

            is BallastDebuggerEventV4.SideJobError -> BallastDebuggerEventV5.SideJobError(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                key = key,
                restartState = restartState,
                stacktrace = stacktrace,
            )

            is BallastDebuggerEventV4.UnhandledError -> BallastDebuggerEventV5.UnhandledError(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                stacktrace = stacktrace,
            )

            is BallastDebuggerEventV4.InterceptorAttached -> BallastDebuggerEventV5.InterceptorAttached(
                connectionId = connectionId,
                viewModelName = viewModelName,
                uuid = uuid,
                timestamp = timestamp,
                interceptorType = interceptorType,
                interceptorToStringValue = interceptorToStringValue,
            )
            is BallastDebuggerEventV4.InterceptorFailed -> BallastDebuggerEventV5.InterceptorFailed(
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

    override fun mapAction(action: BallastDebuggerActionV5): BallastDebuggerActionV4 = with(action) {
        return when (this) {
            is BallastDebuggerActionV5.RequestViewModelRefresh -> BallastDebuggerActionV4.RequestViewModelRefresh(
                connectionId = connectionId,
                viewModelName = viewModelName,
            )

            is BallastDebuggerActionV5.RequestRestoreState -> BallastDebuggerActionV4.RequestRestoreState(
                connectionId = connectionId,
                viewModelName = viewModelName,
                stateUuid = stateUuid,
            )

            is BallastDebuggerActionV5.RequestResendInput -> BallastDebuggerActionV4.RequestResendInput(
                connectionId = connectionId,
                viewModelName = viewModelName,
                inputUuid = inputUuid,
            )

            is BallastDebuggerActionV5.RequestReplaceState -> {
                error("RequestReplaceState only supported on clients v5+")
            }

            is BallastDebuggerActionV5.RequestSendInput -> {
                error("RequestSendInput only supported on clients v5+")
            }
        }
    }
}
