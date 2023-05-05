package com.copperleaf.ballast.debugger.server

import com.copperleaf.ballast.SideJobScope
import com.copperleaf.ballast.debugger.versions.v1.BallastDebuggerEventV1
import com.copperleaf.ballast.debugger.versions.v2.BallastDebuggerEventV2
import com.copperleaf.ballast.debugger.versions.v2.ClientModelConverterV1ToV2
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.datetime.LocalDateTime
import java.time.Month

class V1ToV2Test : StringSpec({
    val converter = ClientModelConverterV1ToV2()
    val inputConnectionId = "asdf"
    val inputConnectionBallastVersion = "1.0.0"
    val viewModelName = "TestViewModel"
    val viewModelType = "BasicViewModel"
    val uuid = "qwerty"
    val timestamp = LocalDateTime(2023, Month.JANUARY, 1, 1, 0, 0, 0)
    val inputType = "AnInput"
    val inputToStringValue = "AnInput(one=two)"
    val eventType = "AnEvent"
    val eventToStringValue = "AnEvent(one=two)"
    val stateType = "AState"
    val stateToStringValue = "AState(one=two)"
    val key = "keyOne"
    val restartState = SideJobScope.RestartState.Initial
    val stacktrace = "error at line 12..."

    "Heartbeat" {
        val input = BallastDebuggerEventV1.Heartbeat(inputConnectionId, inputConnectionBallastVersion)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV2.Heartbeat>()
        output.connectionId shouldBe inputConnectionId
        output.connectionBallastVersion shouldBe inputConnectionBallastVersion
    }
    "RefreshViewModelStart" {
        val input = BallastDebuggerEventV1.RefreshViewModelStart(inputConnectionId, viewModelName)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV2.RefreshViewModelStart>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
    }
    "RefreshViewModelComplete" {
        val input = BallastDebuggerEventV1.RefreshViewModelComplete(inputConnectionId, viewModelName)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV2.RefreshViewModelComplete>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
    }
    "ViewModelStarted" {
        val input = BallastDebuggerEventV1.ViewModelStarted(inputConnectionId, viewModelName, viewModelType, uuid, timestamp)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV2.ViewModelStarted>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.viewModelType shouldBe viewModelType
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
    }
    "ViewModelCleared" {
        val input = BallastDebuggerEventV1.ViewModelCleared(inputConnectionId, viewModelName, uuid, timestamp)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV2.ViewModelCleared>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
    }
    "InputQueued" {
        val input = BallastDebuggerEventV1.InputQueued(inputConnectionId, viewModelName, uuid, timestamp, inputType, inputToStringValue)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV2.InputQueued>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.inputType shouldBe inputType
        output.inputToStringValue shouldBe inputToStringValue
    }
    "InputAccepted" {
        val input = BallastDebuggerEventV1.InputAccepted(inputConnectionId, viewModelName, uuid, timestamp, inputType, inputToStringValue)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV2.InputAccepted>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.inputType shouldBe inputType
        output.inputToStringValue shouldBe inputToStringValue
    }
    "InputRejected" {
        val input = BallastDebuggerEventV1.InputRejected(inputConnectionId, viewModelName, uuid, timestamp, inputType, inputToStringValue)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV2.InputRejected>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.inputType shouldBe inputType
        output.inputToStringValue shouldBe inputToStringValue
    }
    "InputDropped" {
        val input = BallastDebuggerEventV1.InputDropped(inputConnectionId, viewModelName, uuid, timestamp, inputType, inputToStringValue)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV2.InputDropped>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.inputType shouldBe inputType
        output.inputToStringValue shouldBe inputToStringValue
    }
    "InputHandledSuccessfully" {
        val input = BallastDebuggerEventV1.InputHandledSuccessfully(inputConnectionId, viewModelName, uuid, timestamp, inputType, inputToStringValue)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV2.InputHandledSuccessfully>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.inputType shouldBe inputType
        output.inputToStringValue shouldBe inputToStringValue
    }
    "InputCancelled" {
        val input = BallastDebuggerEventV1.InputCancelled(inputConnectionId, viewModelName, uuid, timestamp, inputType, inputToStringValue)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV2.InputCancelled>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.inputType shouldBe inputType
        output.inputToStringValue shouldBe inputToStringValue
    }
    "InputHandlerError" {
        val input = BallastDebuggerEventV1.InputHandlerError(inputConnectionId, viewModelName, uuid, timestamp, inputType, inputToStringValue, stacktrace)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV2.InputHandlerError>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.inputType shouldBe inputType
        output.inputToStringValue shouldBe inputToStringValue
        output.stacktrace shouldBe stacktrace
    }
    "EventQueued" {
        val input = BallastDebuggerEventV1.EventQueued(inputConnectionId, viewModelName, uuid, timestamp, eventType, eventToStringValue)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV2.EventQueued>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.eventType shouldBe eventType
        output.eventToStringValue shouldBe eventToStringValue
    }
    "EventEmitted" {
        val input = BallastDebuggerEventV1.EventEmitted(inputConnectionId, viewModelName, uuid, timestamp, eventType, eventToStringValue)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV2.EventEmitted>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.eventType shouldBe eventType
        output.eventToStringValue shouldBe eventToStringValue
    }
    "EventHandledSuccessfully" {
        val input = BallastDebuggerEventV1.EventHandledSuccessfully(inputConnectionId, viewModelName, uuid, timestamp, eventType, eventToStringValue)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV2.EventHandledSuccessfully>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.eventType shouldBe eventType
        output.eventToStringValue shouldBe eventToStringValue
    }
    "EventHandlerError" {
        val input = BallastDebuggerEventV1.EventHandlerError(inputConnectionId, viewModelName, uuid, timestamp, eventType, eventToStringValue, stacktrace)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV2.EventHandlerError>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.eventType shouldBe eventType
        output.eventToStringValue shouldBe eventToStringValue
        output.stacktrace shouldBe stacktrace
    }
    "EventProcessingStarted" {
        val input = BallastDebuggerEventV1.EventProcessingStarted(inputConnectionId, viewModelName, uuid, timestamp)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV2.EventProcessingStarted>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
    }
    "EventProcessingStopped" {
        val input = BallastDebuggerEventV1.EventProcessingStopped(inputConnectionId, viewModelName, uuid, timestamp)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV2.EventProcessingStopped>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
    }
    "StateChanged" {
        val input = BallastDebuggerEventV1.StateChanged(inputConnectionId, viewModelName, uuid, timestamp, stateType, stateToStringValue)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV2.StateChanged>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.stateType shouldBe stateType
        output.stateToStringValue shouldBe stateToStringValue
    }
    "SideJobQueued" {
        val input = BallastDebuggerEventV1.SideJobQueued(inputConnectionId, viewModelName, uuid, timestamp, key)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV2.SideJobQueued>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.key shouldBe key
    }
    "SideJobStarted" {
        val input = BallastDebuggerEventV1.SideJobStarted(inputConnectionId, viewModelName, uuid, timestamp, key, restartState)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV2.SideJobStarted>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.key shouldBe key
        output.restartState shouldBe restartState
    }
    "SideJobCompleted" {
        val input = BallastDebuggerEventV1.SideJobCompleted(inputConnectionId, viewModelName, uuid, timestamp, key, restartState)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV2.SideJobCompleted>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.key shouldBe key
        output.restartState shouldBe restartState
    }
    "SideJobCancelled" {
        val input = BallastDebuggerEventV1.SideJobCancelled(inputConnectionId, viewModelName, uuid, timestamp, key, restartState)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV2.SideJobCancelled>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.key shouldBe key
        output.restartState shouldBe restartState
    }
    "SideJobError" {
        val input = BallastDebuggerEventV1.SideJobError(inputConnectionId, viewModelName, uuid, timestamp, key, restartState, stacktrace)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV2.SideJobError>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.key shouldBe key
        output.restartState shouldBe restartState
        output.stacktrace shouldBe stacktrace
    }
    "UnhandledError" {
        val input = BallastDebuggerEventV1.UnhandledError(inputConnectionId, viewModelName, uuid, timestamp, stacktrace)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV2.UnhandledError>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.stacktrace shouldBe stacktrace
    }
})
