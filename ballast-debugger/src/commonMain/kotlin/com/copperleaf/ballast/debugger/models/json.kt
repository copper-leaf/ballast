package com.copperleaf.ballast.debugger.models

import com.benasher44.uuid.uuid4
import com.copperleaf.ballast.BallastNotification
import kotlinx.serialization.json.Json

public val debuggerEventJson: Json = Json {
    isLenient = true
}

public fun <Inputs : Any, Events : Any, State : Any> BallastNotification<Inputs, Events, State>.serialize(
    connectionId: String,
    uuid: String = uuid4().toString(),
): BallastDebuggerEvent {
    return when (this) {
        is BallastNotification.ViewModelStarted -> {
            BallastDebuggerEvent.ViewModelStarted(connectionId, vm.name, uuid)
        }
        is BallastNotification.ViewModelCleared -> {
            BallastDebuggerEvent.ViewModelCleared(connectionId, vm.name, uuid)
        }
        is BallastNotification.InputAccepted -> {
            BallastDebuggerEvent.InputAccepted(connectionId, vm.name, uuid, input.type, input.toString())
        }
        is BallastNotification.InputRejected -> {
            BallastDebuggerEvent.InputRejected(connectionId, vm.name, uuid, input.type, input.toString())
        }
        is BallastNotification.InputDropped -> {
            BallastDebuggerEvent.InputDropped(connectionId, vm.name, uuid, input.type, input.toString())
        }
        is BallastNotification.InputHandledSuccessfully -> {
            BallastDebuggerEvent.InputHandledSuccessfully(connectionId, vm.name, uuid, input.type, input.toString())
        }
        is BallastNotification.InputCancelled -> {
            BallastDebuggerEvent.InputCancelled(connectionId, vm.name, uuid, input.type, input.toString())
        }
        is BallastNotification.InputHandlerError -> {
            BallastDebuggerEvent.InputHandlerError(connectionId, vm.name, uuid, input.type, input.toString())
        }
        is BallastNotification.EventEmitted -> {
            BallastDebuggerEvent.EventEmitted(connectionId, vm.name, uuid, event.type, event.toString())
        }
        is BallastNotification.EventHandledSuccessfully -> {
            BallastDebuggerEvent.EventHandledSuccessfully(connectionId, vm.name, uuid, event.type, event.toString())
        }
        is BallastNotification.EventHandlerError -> {
            BallastDebuggerEvent.EventHandlerError(connectionId, vm.name, uuid, event.type, event.toString())
        }
        is BallastNotification.EventProcessingStarted -> {
            BallastDebuggerEvent.EventProcessingStarted(connectionId, vm.name, uuid)
        }
        is BallastNotification.EventProcessingStopped -> {
            BallastDebuggerEvent.EventProcessingStopped(connectionId, vm.name, uuid)
        }
        is BallastNotification.StateChanged -> {
            BallastDebuggerEvent.StateChanged(connectionId, vm.name, uuid, state.toString())
        }
        is BallastNotification.SideEffectStarted -> {
            BallastDebuggerEvent.SideEffectStarted(connectionId, vm.name, uuid, key, restartState)
        }
        is BallastNotification.SideEffectCompleted -> {
            BallastDebuggerEvent.SideEffectCompleted(connectionId, vm.name, uuid, key)
        }
        is BallastNotification.SideEffectError -> {
            BallastDebuggerEvent.SideEffectError(connectionId, vm.name, uuid, key)
        }
        is BallastNotification.UnhandledError -> {
            BallastDebuggerEvent.UnhandledError(connectionId, vm.name, uuid)
        }
    }
}

private val Any.type: String get() = this::class.simpleName ?: ""
