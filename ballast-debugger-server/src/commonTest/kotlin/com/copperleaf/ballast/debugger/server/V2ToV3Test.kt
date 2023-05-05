package com.copperleaf.ballast.debugger.server

import com.copperleaf.ballast.SideJobScope
import com.copperleaf.ballast.debugger.versions.v2.BallastDebuggerEventV2
import com.copperleaf.ballast.debugger.versions.v3.BallastDebuggerEventV3
import com.copperleaf.ballast.debugger.versions.v3.ClientModelConverterV2ToV3
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.datetime.LocalDateTime
import java.time.Month

class V2ToV3Test : StringSpec({
    val converter = ClientModelConverterV2ToV3()
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
        val input = BallastDebuggerEventV2.Heartbeat(inputConnectionId, inputConnectionBallastVersion)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV3.Heartbeat>()
        output.connectionId shouldBe inputConnectionId
        output.connectionBallastVersion shouldBe inputConnectionBallastVersion
    }
    "RefreshViewModelStart" {
        val input = BallastDebuggerEventV2.RefreshViewModelStart(inputConnectionId, viewModelName)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV3.RefreshViewModelStart>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
    }
    "RefreshViewModelComplete" {
        val input = BallastDebuggerEventV2.RefreshViewModelComplete(inputConnectionId, viewModelName)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV3.RefreshViewModelComplete>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
    }
    "ViewModelStarted" {
        val input = BallastDebuggerEventV2.ViewModelStarted(inputConnectionId, viewModelName, viewModelType, uuid, timestamp)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV3.ViewModelStatusChanged>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.viewModelType shouldBe viewModelType
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.status shouldBe BallastDebuggerEventV3.StatusV3.Running
    }
    "ViewModelCleared" {
        val input = BallastDebuggerEventV2.ViewModelCleared(inputConnectionId, viewModelName, uuid, timestamp)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV3.ViewModelStatusChanged>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.viewModelType shouldBe ""
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.status shouldBe BallastDebuggerEventV3.StatusV3.Cleared
    }
    "InputQueued" {
        val input = BallastDebuggerEventV2.InputQueued(inputConnectionId, viewModelName, uuid, timestamp, inputType, inputToStringValue)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV3.InputQueued>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.inputType shouldBe inputType
        output.serializedInput shouldBe inputToStringValue
        output.inputContentType shouldBe "text/*"
    }
    "InputAccepted" {
        val input = BallastDebuggerEventV2.InputAccepted(inputConnectionId, viewModelName, uuid, timestamp, inputType, inputToStringValue)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV3.InputAccepted>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.inputType shouldBe inputType
        output.serializedInput shouldBe inputToStringValue
        output.inputContentType shouldBe "text/*"
    }
    "InputRejected" {
        val input = BallastDebuggerEventV2.InputRejected(inputConnectionId, viewModelName, uuid, timestamp, inputType, inputToStringValue)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV3.InputRejected>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.inputType shouldBe inputType
        output.serializedInput shouldBe inputToStringValue
        output.inputContentType shouldBe "text/*"
    }
    "InputDropped" {
        val input = BallastDebuggerEventV2.InputDropped(inputConnectionId, viewModelName, uuid, timestamp, inputType, inputToStringValue)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV3.InputDropped>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.inputType shouldBe inputType
        output.serializedInput shouldBe inputToStringValue
        output.inputContentType shouldBe "text/*"
    }
    "InputHandledSuccessfully" {
        val input = BallastDebuggerEventV2.InputHandledSuccessfully(inputConnectionId, viewModelName, uuid, timestamp, inputType, inputToStringValue)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV3.InputHandledSuccessfully>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.inputType shouldBe inputType
        output.serializedInput shouldBe inputToStringValue
        output.inputContentType shouldBe "text/*"
    }
    "InputCancelled" {
        val input = BallastDebuggerEventV2.InputCancelled(inputConnectionId, viewModelName, uuid, timestamp, inputType, inputToStringValue)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV3.InputCancelled>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.inputType shouldBe inputType
        output.serializedInput shouldBe inputToStringValue
        output.inputContentType shouldBe "text/*"
    }
    "InputHandlerError" {
        val input = BallastDebuggerEventV2.InputHandlerError(inputConnectionId, viewModelName, uuid, timestamp, inputType, inputToStringValue, stacktrace)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV3.InputHandlerError>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.inputType shouldBe inputType
        output.serializedInput shouldBe inputToStringValue
        output.inputContentType shouldBe "text/*"
        output.stacktrace shouldBe stacktrace
    }
    "EventQueued" {
        val input = BallastDebuggerEventV2.EventQueued(inputConnectionId, viewModelName, uuid, timestamp, eventType, eventToStringValue)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV3.EventQueued>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.eventType shouldBe eventType
        output.serializedEvent shouldBe eventToStringValue
        output.eventContentType shouldBe "text/*"
    }
    "EventEmitted" {
        val input = BallastDebuggerEventV2.EventEmitted(inputConnectionId, viewModelName, uuid, timestamp, eventType, eventToStringValue)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV3.EventEmitted>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.eventType shouldBe eventType
        output.serializedEvent shouldBe eventToStringValue
        output.eventContentType shouldBe "text/*"
    }
    "EventHandledSuccessfully" {
        val input = BallastDebuggerEventV2.EventHandledSuccessfully(inputConnectionId, viewModelName, uuid, timestamp, eventType, eventToStringValue)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV3.EventHandledSuccessfully>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.eventType shouldBe eventType
        output.serializedEvent shouldBe eventToStringValue
        output.eventContentType shouldBe "text/*"
    }
    "EventHandlerError" {
        val input = BallastDebuggerEventV2.EventHandlerError(inputConnectionId, viewModelName, uuid, timestamp, eventType, eventToStringValue, stacktrace)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV3.EventHandlerError>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.eventType shouldBe eventType
        output.serializedEvent shouldBe eventToStringValue
        output.eventContentType shouldBe "text/*"
        output.stacktrace shouldBe stacktrace
    }
    "EventProcessingStarted" {
        val input = BallastDebuggerEventV2.EventProcessingStarted(inputConnectionId, viewModelName, uuid, timestamp)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV3.EventProcessingStarted>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
    }
    "EventProcessingStopped" {
        val input = BallastDebuggerEventV2.EventProcessingStopped(inputConnectionId, viewModelName, uuid, timestamp)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV3.EventProcessingStopped>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
    }
    "StateChanged" {
        val input = BallastDebuggerEventV2.StateChanged(inputConnectionId, viewModelName, uuid, timestamp, stateType, stateToStringValue)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV3.StateChanged>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.stateType shouldBe stateType
        output.serializedState shouldBe stateToStringValue
        output.stateContentType shouldBe "text/*"
    }
    "SideJobQueued" {
        val input = BallastDebuggerEventV2.SideJobQueued(inputConnectionId, viewModelName, uuid, timestamp, key)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV3.SideJobQueued>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.key shouldBe key
    }
    "SideJobStarted" {
        val input = BallastDebuggerEventV2.SideJobStarted(inputConnectionId, viewModelName, uuid, timestamp, key, restartState)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV3.SideJobStarted>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.key shouldBe key
        output.restartState shouldBe restartState
    }
    "SideJobCompleted" {
        val input = BallastDebuggerEventV2.SideJobCompleted(inputConnectionId, viewModelName, uuid, timestamp, key, restartState)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV3.SideJobCompleted>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.key shouldBe key
        output.restartState shouldBe restartState
    }
    "SideJobCancelled" {
        val input = BallastDebuggerEventV2.SideJobCancelled(inputConnectionId, viewModelName, uuid, timestamp, key, restartState)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV3.SideJobCancelled>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.key shouldBe key
        output.restartState shouldBe restartState
    }
    "SideJobError" {
        val input = BallastDebuggerEventV2.SideJobError(inputConnectionId, viewModelName, uuid, timestamp, key, restartState, stacktrace)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV3.SideJobError>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.key shouldBe key
        output.restartState shouldBe restartState
        output.stacktrace shouldBe stacktrace
    }
    "UnhandledError" {
        val input = BallastDebuggerEventV2.UnhandledError(inputConnectionId, viewModelName, uuid, timestamp, stacktrace)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV3.UnhandledError>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.stacktrace shouldBe stacktrace
    }
})
