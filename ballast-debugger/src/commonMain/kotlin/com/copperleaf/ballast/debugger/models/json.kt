package com.copperleaf.ballast.debugger.models

import com.copperleaf.ballast.BallastNotification
import com.copperleaf.ballast.debugger.BallastDebuggerViewModelConnection
import com.copperleaf.ballast.debugger.versions.v3.BallastDebuggerEventV3
import com.copperleaf.ballast.internal.Status
import io.ktor.http.ContentType
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json

public val debuggerEventJson: Json = Json {
    isLenient = true
}

internal fun <Inputs : Any, Events : Any, State : Any> BallastNotification<Inputs, Events, State>.serialize(
    connectionId: String,
    viewModelConnection: BallastDebuggerViewModelConnection<Inputs, Events, State>,
    uuid: String,
    firstSeen: LocalDateTime,
    now: LocalDateTime,
): BallastDebuggerEventV3 {
    return when (this) {
        is BallastNotification.ViewModelStatusChanged -> {
            BallastDebuggerEventV3.ViewModelStatusChanged(connectionId, viewModelName, viewModelType, uuid, firstSeen, status.serialize())
        }
        is BallastNotification.InputQueued -> {
            val (contentType, serializedContent) = viewModelConnection.serializeInput(input)
            BallastDebuggerEventV3.InputQueued(connectionId, viewModelName, uuid, firstSeen, input.type, serializedContent, contentType.asContentTypeString())
        }
        is BallastNotification.InputAccepted -> {
            val (contentType, serializedContent) = viewModelConnection.serializeInput(input)
            BallastDebuggerEventV3.InputAccepted(connectionId, viewModelName, uuid, now, input.type, serializedContent, contentType.asContentTypeString())
        }
        is BallastNotification.InputRejected -> {
            val (contentType, serializedContent) = viewModelConnection.serializeInput(input)
            BallastDebuggerEventV3.InputRejected(connectionId, viewModelName, uuid, now, input.type, serializedContent, contentType.asContentTypeString())
        }
        is BallastNotification.InputDropped -> {
            val (contentType, serializedContent) = viewModelConnection.serializeInput(input)
            BallastDebuggerEventV3.InputDropped(connectionId, viewModelName, uuid, now, input.type, serializedContent, contentType.asContentTypeString())
        }
        is BallastNotification.InputHandledSuccessfully -> {
            val (contentType, serializedContent) = viewModelConnection.serializeInput(input)
            BallastDebuggerEventV3.InputHandledSuccessfully(connectionId, viewModelName, uuid, now, input.type, serializedContent, contentType.asContentTypeString())
        }
        is BallastNotification.InputCancelled -> {
            val (contentType, serializedContent) = viewModelConnection.serializeInput(input)
            BallastDebuggerEventV3.InputCancelled(connectionId, viewModelName, uuid, now, input.type, serializedContent, contentType.asContentTypeString())
        }
        is BallastNotification.InputHandlerError -> {
            val (contentType, serializedContent) = viewModelConnection.serializeInput(input)
            BallastDebuggerEventV3.InputHandlerError(
                connectionId, viewModelName, uuid, now, input.type, serializedContent, contentType.asContentTypeString(),
                throwable.stackTraceToString()
            )
        }
        is BallastNotification.EventQueued -> {
            val (contentType, serializedContent) = viewModelConnection.serializeEvent(event)
            BallastDebuggerEventV3.EventQueued(connectionId, viewModelName, uuid, firstSeen, event.type, serializedContent, contentType.asContentTypeString())
        }
        is BallastNotification.EventEmitted -> {
            val (contentType, serializedContent) = viewModelConnection.serializeEvent(event)
            BallastDebuggerEventV3.EventEmitted(connectionId, viewModelName, uuid, now, event.type, serializedContent, contentType.asContentTypeString())
        }
        is BallastNotification.EventHandledSuccessfully -> {
            val (contentType, serializedContent) = viewModelConnection.serializeEvent(event)
            BallastDebuggerEventV3.EventHandledSuccessfully(connectionId, viewModelName, uuid, now, event.type, serializedContent, contentType.asContentTypeString())
        }
        is BallastNotification.EventHandlerError -> {
            val (contentType, serializedContent) = viewModelConnection.serializeEvent(event)
            BallastDebuggerEventV3.EventHandlerError(
                connectionId, viewModelName, uuid, now, event.type, serializedContent, contentType.asContentTypeString(),
                throwable.stackTraceToString()
            )
        }
        is BallastNotification.EventProcessingStarted -> {
            BallastDebuggerEventV3.EventProcessingStarted(connectionId, viewModelName, uuid, now)
        }
        is BallastNotification.EventProcessingStopped -> {
            BallastDebuggerEventV3.EventProcessingStopped(connectionId, viewModelName, uuid, now)
        }
        is BallastNotification.StateChanged -> {
            val (contentType, serializedContent) = viewModelConnection.serializeState(state)
            BallastDebuggerEventV3.StateChanged(connectionId, viewModelName, uuid, firstSeen, state.type, serializedContent, contentType.asContentTypeString())
        }

        is BallastNotification.SideJobQueued -> {
            BallastDebuggerEventV3.SideJobQueued(connectionId, viewModelName, uuid, firstSeen, key)
        }
        is BallastNotification.SideJobStarted -> {
            BallastDebuggerEventV3.SideJobStarted(connectionId, viewModelName, uuid, now, key, restartState)
        }
        is BallastNotification.SideJobCompleted -> {
            BallastDebuggerEventV3.SideJobCompleted(connectionId, viewModelName, uuid, now, key, restartState)
        }
        is BallastNotification.SideJobCancelled -> {
            BallastDebuggerEventV3.SideJobCancelled(connectionId, viewModelName, uuid, now, key, restartState)
        }
        is BallastNotification.SideJobError -> {
            BallastDebuggerEventV3.SideJobError(
                connectionId, viewModelName, uuid, now, key, restartState,
                throwable.stackTraceToString()
            )
        }
        is BallastNotification.UnhandledError -> {
            BallastDebuggerEventV3.UnhandledError(
                connectionId, viewModelName, uuid, now,
                throwable.stackTraceToString()
            )
        }
        is BallastNotification.InterceptorAttached -> {
            BallastDebuggerEventV3.InterceptorAttached(connectionId, viewModelName, uuid, now, interceptor.type, interceptor.toString())
        }
        is BallastNotification.InterceptorFailed-> {
            BallastDebuggerEventV3.InterceptorFailed(connectionId, viewModelName, uuid, now, interceptor.type, interceptor.toString(), throwable.stackTraceToString())
        }
    }
}

public fun <Inputs : Any, Events : Any, State : Any> BallastNotification<Inputs, Events, State>.getActualValue(): Any? {
    return when (this) {
        is BallastNotification.InputQueued -> input
        is BallastNotification.InputAccepted -> input
        is BallastNotification.InputRejected -> input
        is BallastNotification.InputDropped -> input
        is BallastNotification.InputHandledSuccessfully -> input
        is BallastNotification.InputCancelled -> input
        is BallastNotification.InputHandlerError -> input
        is BallastNotification.EventQueued -> event
        is BallastNotification.EventEmitted -> event
        is BallastNotification.EventHandledSuccessfully -> event
        is BallastNotification.EventHandlerError -> event
        is BallastNotification.StateChanged -> state
        else -> null
    }
}

private val Any.type: String get() = this::class.simpleName ?: ""

public fun Status.serialize(): BallastDebuggerEventV3.StatusV3 {
    return when(this) {
        is Status.NotStarted -> BallastDebuggerEventV3.StatusV3.NotStarted
        is Status.Running -> BallastDebuggerEventV3.StatusV3.Running
        is Status.ShuttingDown -> BallastDebuggerEventV3.StatusV3.ShuttingDown
        is Status.Cleared -> BallastDebuggerEventV3.StatusV3.Cleared
    }
}

private fun ContentType.asContentTypeString(): String {
    return "$contentType/$contentSubtype"
}
