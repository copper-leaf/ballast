package com.copperleaf.ballast.debugger.models

import com.copperleaf.ballast.BallastNotification
import com.copperleaf.ballast.debugger.BallastDebuggerViewModelConnection
import com.copperleaf.ballast.debugger.versions.v5.BallastDebuggerEventV5
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
): BallastDebuggerEventV5 {
    return when (this) {
        is BallastNotification.ViewModelStatusChanged -> {
            BallastDebuggerEventV5.ViewModelStatusChanged(connectionId, viewModelName, viewModelType, uuid, firstSeen, status.serialize())
        }
        is BallastNotification.InputQueued -> {
            val (contentType, serializedContent) = viewModelConnection.adapter.serializeInput(input)
            BallastDebuggerEventV5.InputQueued(connectionId, viewModelName, uuid, firstSeen, input.type, serializedContent, contentType.asContentTypeString())
        }
        is BallastNotification.InputAccepted -> {
            val (contentType, serializedContent) = viewModelConnection.adapter.serializeInput(input)
            BallastDebuggerEventV5.InputAccepted(connectionId, viewModelName, uuid, now, input.type, serializedContent, contentType.asContentTypeString())
        }
        is BallastNotification.InputRejected -> {
            val (contentType, serializedContent) = viewModelConnection.adapter.serializeInput(input)
            BallastDebuggerEventV5.InputRejected(connectionId, viewModelName, uuid, now, input.type, serializedContent, contentType.asContentTypeString())
        }
        is BallastNotification.InputDropped -> {
            val (contentType, serializedContent) = viewModelConnection.adapter.serializeInput(input)
            BallastDebuggerEventV5.InputDropped(connectionId, viewModelName, uuid, now, input.type, serializedContent, contentType.asContentTypeString())
        }
        is BallastNotification.InputHandledSuccessfully -> {
            val (contentType, serializedContent) = viewModelConnection.adapter.serializeInput(input)
            BallastDebuggerEventV5.InputHandledSuccessfully(connectionId, viewModelName, uuid, now, input.type, serializedContent, contentType.asContentTypeString())
        }
        is BallastNotification.InputCancelled -> {
            val (contentType, serializedContent) = viewModelConnection.adapter.serializeInput(input)
            BallastDebuggerEventV5.InputCancelled(connectionId, viewModelName, uuid, now, input.type, serializedContent, contentType.asContentTypeString())
        }
        is BallastNotification.InputHandlerError -> {
            val (contentType, serializedContent) = viewModelConnection.adapter.serializeInput(input)
            BallastDebuggerEventV5.InputHandlerError(
                connectionId, viewModelName, uuid, now, input.type, serializedContent, contentType.asContentTypeString(),
                throwable.stackTraceToString()
            )
        }
        is BallastNotification.EventQueued -> {
            val (contentType, serializedContent) = viewModelConnection.adapter.serializeEvent(event)
            BallastDebuggerEventV5.EventQueued(connectionId, viewModelName, uuid, firstSeen, event.type, serializedContent, contentType.asContentTypeString())
        }
        is BallastNotification.EventEmitted -> {
            val (contentType, serializedContent) = viewModelConnection.adapter.serializeEvent(event)
            BallastDebuggerEventV5.EventEmitted(connectionId, viewModelName, uuid, now, event.type, serializedContent, contentType.asContentTypeString())
        }
        is BallastNotification.EventHandledSuccessfully -> {
            val (contentType, serializedContent) = viewModelConnection.adapter.serializeEvent(event)
            BallastDebuggerEventV5.EventHandledSuccessfully(connectionId, viewModelName, uuid, now, event.type, serializedContent, contentType.asContentTypeString())
        }
        is BallastNotification.EventHandlerError -> {
            val (contentType, serializedContent) = viewModelConnection.adapter.serializeEvent(event)
            BallastDebuggerEventV5.EventHandlerError(
                connectionId, viewModelName, uuid, now, event.type, serializedContent, contentType.asContentTypeString(),
                throwable.stackTraceToString()
            )
        }
        is BallastNotification.EventProcessingStarted -> {
            BallastDebuggerEventV5.EventProcessingStarted(connectionId, viewModelName, uuid, now)
        }
        is BallastNotification.EventProcessingStopped -> {
            BallastDebuggerEventV5.EventProcessingStopped(connectionId, viewModelName, uuid, now)
        }
        is BallastNotification.StateChanged -> {
            val (contentType, serializedContent) = viewModelConnection.adapter.serializeState(state)
            BallastDebuggerEventV5.StateChanged(connectionId, viewModelName, uuid, firstSeen, state.type, serializedContent, contentType.asContentTypeString())
        }

        is BallastNotification.SideJobQueued -> {
            BallastDebuggerEventV5.SideJobQueued(connectionId, viewModelName, uuid, firstSeen, key)
        }
        is BallastNotification.SideJobStarted -> {
            BallastDebuggerEventV5.SideJobStarted(connectionId, viewModelName, uuid, now, key, restartState)
        }
        is BallastNotification.SideJobCompleted -> {
            BallastDebuggerEventV5.SideJobCompleted(connectionId, viewModelName, uuid, now, key, restartState)
        }
        is BallastNotification.SideJobCancelled -> {
            BallastDebuggerEventV5.SideJobCancelled(connectionId, viewModelName, uuid, now, key, restartState)
        }
        is BallastNotification.SideJobError -> {
            BallastDebuggerEventV5.SideJobError(
                connectionId, viewModelName, uuid, now, key, restartState,
                throwable.stackTraceToString()
            )
        }
        is BallastNotification.UnhandledError -> {
            BallastDebuggerEventV5.UnhandledError(
                connectionId, viewModelName, uuid, now,
                throwable.stackTraceToString()
            )
        }
        is BallastNotification.InterceptorAttached -> {
            BallastDebuggerEventV5.InterceptorAttached(connectionId, viewModelName, uuid, now, interceptor.type, interceptor.toString())
        }
        is BallastNotification.InterceptorFailed-> {
            BallastDebuggerEventV5.InterceptorFailed(connectionId, viewModelName, uuid, now, interceptor.type, interceptor.toString(), throwable.stackTraceToString())
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

public fun Status.serialize(): BallastDebuggerEventV5.StatusV5 {
    return when(this) {
        is Status.NotStarted -> BallastDebuggerEventV5.StatusV5.NotStarted
        is Status.Running -> BallastDebuggerEventV5.StatusV5.Running
        is Status.ShuttingDown -> BallastDebuggerEventV5.StatusV5.ShuttingDown
        is Status.Cleared -> BallastDebuggerEventV5.StatusV5.Cleared
    }
}

private fun ContentType.asContentTypeString(): String {
    return "$contentType/$contentSubtype"
}
