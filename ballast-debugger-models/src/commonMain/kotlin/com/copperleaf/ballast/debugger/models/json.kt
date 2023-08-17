package com.copperleaf.ballast.debugger.models

import com.copperleaf.ballast.BallastNotification
import com.copperleaf.ballast.debugger.BallastDebuggerViewModelConnection
import com.copperleaf.ballast.debugger.versions.v4.BallastDebuggerEventV4
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
): BallastDebuggerEventV4 {
    return when (this) {
        is BallastNotification.ViewModelStatusChanged -> {
            BallastDebuggerEventV4.ViewModelStatusChanged(connectionId, viewModelName, viewModelType, uuid, firstSeen, status.serialize())
        }
        is BallastNotification.InputQueued -> {
            val (contentType, serializedContent) = viewModelConnection.serializeInput(input)
            BallastDebuggerEventV4.InputQueued(connectionId, viewModelName, uuid, firstSeen, input.type, serializedContent, contentType.asContentTypeString())
        }
        is BallastNotification.InputAccepted -> {
            val (contentType, serializedContent) = viewModelConnection.serializeInput(input)
            BallastDebuggerEventV4.InputAccepted(connectionId, viewModelName, uuid, now, input.type, serializedContent, contentType.asContentTypeString())
        }
        is BallastNotification.InputRejected -> {
            val (contentType, serializedContent) = viewModelConnection.serializeInput(input)
            BallastDebuggerEventV4.InputRejected(connectionId, viewModelName, uuid, now, input.type, serializedContent, contentType.asContentTypeString())
        }
        is BallastNotification.InputDropped -> {
            val (contentType, serializedContent) = viewModelConnection.serializeInput(input)
            BallastDebuggerEventV4.InputDropped(connectionId, viewModelName, uuid, now, input.type, serializedContent, contentType.asContentTypeString())
        }
        is BallastNotification.InputHandledSuccessfully -> {
            val (contentType, serializedContent) = viewModelConnection.serializeInput(input)
            BallastDebuggerEventV4.InputHandledSuccessfully(connectionId, viewModelName, uuid, now, input.type, serializedContent, contentType.asContentTypeString())
        }
        is BallastNotification.InputCancelled -> {
            val (contentType, serializedContent) = viewModelConnection.serializeInput(input)
            BallastDebuggerEventV4.InputCancelled(connectionId, viewModelName, uuid, now, input.type, serializedContent, contentType.asContentTypeString())
        }
        is BallastNotification.InputHandlerError -> {
            val (contentType, serializedContent) = viewModelConnection.serializeInput(input)
            BallastDebuggerEventV4.InputHandlerError(
                connectionId, viewModelName, uuid, now, input.type, serializedContent, contentType.asContentTypeString(),
                throwable.stackTraceToString()
            )
        }
        is BallastNotification.EventQueued -> {
            val (contentType, serializedContent) = viewModelConnection.serializeEvent(event)
            BallastDebuggerEventV4.EventQueued(connectionId, viewModelName, uuid, firstSeen, event.type, serializedContent, contentType.asContentTypeString())
        }
        is BallastNotification.EventEmitted -> {
            val (contentType, serializedContent) = viewModelConnection.serializeEvent(event)
            BallastDebuggerEventV4.EventEmitted(connectionId, viewModelName, uuid, now, event.type, serializedContent, contentType.asContentTypeString())
        }
        is BallastNotification.EventHandledSuccessfully -> {
            val (contentType, serializedContent) = viewModelConnection.serializeEvent(event)
            BallastDebuggerEventV4.EventHandledSuccessfully(connectionId, viewModelName, uuid, now, event.type, serializedContent, contentType.asContentTypeString())
        }
        is BallastNotification.EventHandlerError -> {
            val (contentType, serializedContent) = viewModelConnection.serializeEvent(event)
            BallastDebuggerEventV4.EventHandlerError(
                connectionId, viewModelName, uuid, now, event.type, serializedContent, contentType.asContentTypeString(),
                throwable.stackTraceToString()
            )
        }
        is BallastNotification.EventProcessingStarted -> {
            BallastDebuggerEventV4.EventProcessingStarted(connectionId, viewModelName, uuid, now)
        }
        is BallastNotification.EventProcessingStopped -> {
            BallastDebuggerEventV4.EventProcessingStopped(connectionId, viewModelName, uuid, now)
        }
        is BallastNotification.StateChanged -> {
            val (contentType, serializedContent) = viewModelConnection.serializeState(state)
            BallastDebuggerEventV4.StateChanged(connectionId, viewModelName, uuid, firstSeen, state.type, serializedContent, contentType.asContentTypeString())
        }

        is BallastNotification.SideJobQueued -> {
            BallastDebuggerEventV4.SideJobQueued(connectionId, viewModelName, uuid, firstSeen, key)
        }
        is BallastNotification.SideJobStarted -> {
            BallastDebuggerEventV4.SideJobStarted(connectionId, viewModelName, uuid, now, key, restartState)
        }
        is BallastNotification.SideJobCompleted -> {
            BallastDebuggerEventV4.SideJobCompleted(connectionId, viewModelName, uuid, now, key, restartState)
        }
        is BallastNotification.SideJobCancelled -> {
            BallastDebuggerEventV4.SideJobCancelled(connectionId, viewModelName, uuid, now, key, restartState)
        }
        is BallastNotification.SideJobError -> {
            BallastDebuggerEventV4.SideJobError(
                connectionId, viewModelName, uuid, now, key, restartState,
                throwable.stackTraceToString()
            )
        }
        is BallastNotification.UnhandledError -> {
            BallastDebuggerEventV4.UnhandledError(
                connectionId, viewModelName, uuid, now,
                throwable.stackTraceToString()
            )
        }
        is BallastNotification.InterceptorAttached -> {
            BallastDebuggerEventV4.InterceptorAttached(connectionId, viewModelName, uuid, now, interceptor.type, interceptor.toString())
        }
        is BallastNotification.InterceptorFailed-> {
            BallastDebuggerEventV4.InterceptorFailed(connectionId, viewModelName, uuid, now, interceptor.type, interceptor.toString(), throwable.stackTraceToString())
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

public fun Status.serialize(): BallastDebuggerEventV4.StatusV4 {
    return when(this) {
        is Status.NotStarted -> BallastDebuggerEventV4.StatusV4.NotStarted
        is Status.Running -> BallastDebuggerEventV4.StatusV4.Running
        is Status.ShuttingDown -> BallastDebuggerEventV4.StatusV4.ShuttingDown
        is Status.Cleared -> BallastDebuggerEventV4.StatusV4.Cleared
    }
}

private fun ContentType.asContentTypeString(): String {
    return "$contentType/$contentSubtype"
}
