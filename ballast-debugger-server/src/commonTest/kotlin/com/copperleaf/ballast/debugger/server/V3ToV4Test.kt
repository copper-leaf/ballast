package com.copperleaf.ballast.debugger.server

import com.copperleaf.ballast.SideJobScope
import com.copperleaf.ballast.debugger.versions.v3.BallastDebuggerEventV3
import com.copperleaf.ballast.debugger.versions.v4.BallastDebuggerEventV4
import com.copperleaf.ballast.debugger.versions.v4.ClientModelConverterV3ToV4
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.datetime.LocalDateTime
import java.time.Month

class V4ToV4Test : StringSpec({
    val converter = ClientModelConverterV3ToV4()
    val inputConnectionId = "asdf"
    val inputConnectionBallastVersion = "1.0.0"
    val viewModelName = "TestViewModel"
    val viewModelType = "BasicViewModel"
    val uuid = "qwerty"
    val timestamp = LocalDateTime(2023, Month.JANUARY, 1, 1, 0, 0, 0)
    val inputType = "AnInput"
    val inputToStringValue = "AnInput(one=two)"
    val inputContentType = "text/*"
    val eventType = "AnEvent"
    val eventToStringValue = "AnEvent(one=two)"
    val eventContentType = "text/*"
    val stateType = "AState"
    val stateToStringValue = "AState(one=two)"
    val stateContentType = "text/*"
    val interceptorType = "AnInterceptor"
    val interceptorToStringValue = "AnInterceptor()"
    val key = "keyOne"
    val restartState = SideJobScope.RestartState.Initial
    val stacktrace = "error at line 12..."
    val statusV3 = BallastDebuggerEventV3.StatusV3.Running
    val statusV4 = BallastDebuggerEventV4.StatusV4.Running

    "Heartbeat" {
        val input = BallastDebuggerEventV3.Heartbeat(inputConnectionId, inputConnectionBallastVersion)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV4.Heartbeat>()
        output.connectionId shouldBe inputConnectionId
        output.connectionBallastVersion shouldBe inputConnectionBallastVersion
    }
    "RefreshViewModelStart" {
        val input = BallastDebuggerEventV3.RefreshViewModelStart(inputConnectionId, viewModelName)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV4.RefreshViewModelStart>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
    }
    "RefreshViewModelComplete" {
        val input = BallastDebuggerEventV3.RefreshViewModelComplete(inputConnectionId, viewModelName)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV4.RefreshViewModelComplete>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
    }
    "ViewModelStatusChanged" {
        val input = BallastDebuggerEventV3.ViewModelStatusChanged(inputConnectionId, viewModelName, viewModelType, uuid, timestamp, statusV3)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV4.ViewModelStatusChanged>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.viewModelType shouldBe viewModelType
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.status shouldBe statusV4
    }
    "InputQueued" {
        val input = BallastDebuggerEventV3.InputQueued(inputConnectionId, viewModelName, uuid, timestamp, inputType, inputToStringValue, inputContentType)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV4.InputQueued>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.inputType shouldBe inputType
        output.serializedInput shouldBe inputToStringValue
        output.inputContentType shouldBe inputContentType
    }
    "InputAccepted" {
        val input = BallastDebuggerEventV3.InputAccepted(inputConnectionId, viewModelName, uuid, timestamp, inputType, inputToStringValue, inputContentType)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV4.InputAccepted>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.inputType shouldBe inputType
        output.serializedInput shouldBe inputToStringValue
        output.inputContentType shouldBe inputContentType
    }
    "InputRejected" {
        val input = BallastDebuggerEventV3.InputRejected(inputConnectionId, viewModelName, uuid, timestamp, inputType, inputToStringValue, inputContentType)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV4.InputRejected>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.inputType shouldBe inputType
        output.serializedInput shouldBe inputToStringValue
        output.inputContentType shouldBe inputContentType
    }
    "InputDropped" {
        val input = BallastDebuggerEventV3.InputDropped(inputConnectionId, viewModelName, uuid, timestamp, inputType, inputToStringValue, inputContentType)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV4.InputDropped>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.inputType shouldBe inputType
        output.serializedInput shouldBe inputToStringValue
        output.inputContentType shouldBe inputContentType
    }
    "InputHandledSuccessfully" {
        val input = BallastDebuggerEventV3.InputHandledSuccessfully(inputConnectionId, viewModelName, uuid, timestamp, inputType, inputToStringValue, inputContentType)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV4.InputHandledSuccessfully>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.inputType shouldBe inputType
        output.serializedInput shouldBe inputToStringValue
        output.inputContentType shouldBe inputContentType
    }
    "InputCancelled" {
        val input = BallastDebuggerEventV3.InputCancelled(inputConnectionId, viewModelName, uuid, timestamp, inputType, inputToStringValue, inputContentType)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV4.InputCancelled>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.inputType shouldBe inputType
        output.serializedInput shouldBe inputToStringValue
        output.inputContentType shouldBe inputContentType
    }
    "InputHandlerError" {
        val input = BallastDebuggerEventV3.InputHandlerError(inputConnectionId, viewModelName, uuid, timestamp, inputType, inputToStringValue, inputContentType, stacktrace)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV4.InputHandlerError>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.inputType shouldBe inputType
        output.serializedInput shouldBe inputToStringValue
        output.inputContentType shouldBe inputContentType
        output.stacktrace shouldBe stacktrace
    }
    "EventQueued" {
        val input = BallastDebuggerEventV3.EventQueued(inputConnectionId, viewModelName, uuid, timestamp, eventType, eventToStringValue, eventContentType)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV4.EventQueued>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.eventType shouldBe eventType
        output.serializedEvent shouldBe eventToStringValue
        output.eventContentType shouldBe eventContentType
    }
    "EventEmitted" {
        val input = BallastDebuggerEventV3.EventEmitted(inputConnectionId, viewModelName, uuid, timestamp, eventType, eventToStringValue, eventContentType)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV4.EventEmitted>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.eventType shouldBe eventType
        output.serializedEvent shouldBe eventToStringValue
        output.eventContentType shouldBe eventContentType
    }
    "EventHandledSuccessfully" {
        val input = BallastDebuggerEventV3.EventHandledSuccessfully(inputConnectionId, viewModelName, uuid, timestamp, eventType, eventToStringValue, eventContentType)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV4.EventHandledSuccessfully>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.eventType shouldBe eventType
        output.serializedEvent shouldBe eventToStringValue
        output.eventContentType shouldBe eventContentType
    }
    "EventHandlerError" {
        val input = BallastDebuggerEventV3.EventHandlerError(inputConnectionId, viewModelName, uuid, timestamp, eventType, eventToStringValue, eventContentType, stacktrace)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV4.EventHandlerError>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.eventType shouldBe eventType
        output.serializedEvent shouldBe eventToStringValue
        output.eventContentType shouldBe eventContentType
        output.stacktrace shouldBe stacktrace
    }
    "EventProcessingStarted" {
        val input = BallastDebuggerEventV3.EventProcessingStarted(inputConnectionId, viewModelName, uuid, timestamp)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV4.EventProcessingStarted>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
    }
    "EventProcessingStopped" {
        val input = BallastDebuggerEventV3.EventProcessingStopped(inputConnectionId, viewModelName, uuid, timestamp)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV4.EventProcessingStopped>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
    }
    "StateChanged" {
        val input = BallastDebuggerEventV3.StateChanged(inputConnectionId, viewModelName, uuid, timestamp, stateType, stateToStringValue, stateContentType)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV4.StateChanged>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.stateType shouldBe stateType
        output.serializedState shouldBe stateToStringValue
        output.stateContentType shouldBe stateContentType
    }
    "SideJobQueued" {
        val input = BallastDebuggerEventV3.SideJobQueued(inputConnectionId, viewModelName, uuid, timestamp, key)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV4.SideJobQueued>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.key shouldBe key
    }
    "SideJobStarted" {
        val input = BallastDebuggerEventV3.SideJobStarted(inputConnectionId, viewModelName, uuid, timestamp, key, restartState)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV4.SideJobStarted>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.key shouldBe key
        output.restartState shouldBe restartState
    }
    "SideJobCompleted" {
        val input = BallastDebuggerEventV3.SideJobCompleted(inputConnectionId, viewModelName, uuid, timestamp, key, restartState)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV4.SideJobCompleted>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.key shouldBe key
        output.restartState shouldBe restartState
    }
    "SideJobCancelled" {
        val input = BallastDebuggerEventV3.SideJobCancelled(inputConnectionId, viewModelName, uuid, timestamp, key, restartState)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV4.SideJobCancelled>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.key shouldBe key
        output.restartState shouldBe restartState
    }
    "SideJobError" {
        val input = BallastDebuggerEventV3.SideJobError(inputConnectionId, viewModelName, uuid, timestamp, key, restartState, stacktrace)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV4.SideJobError>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.key shouldBe key
        output.restartState shouldBe restartState
        output.stacktrace shouldBe stacktrace
    }
    "UnhandledError" {
        val input = BallastDebuggerEventV3.UnhandledError(inputConnectionId, viewModelName, uuid, timestamp, stacktrace)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV4.UnhandledError>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.stacktrace shouldBe stacktrace
    }
    "InterceptorAttached" {
        val input = BallastDebuggerEventV3.InterceptorAttached(inputConnectionId, viewModelName, uuid, timestamp, interceptorType, interceptorToStringValue)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV4.InterceptorAttached>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.interceptorType shouldBe interceptorType
        output.interceptorToStringValue shouldBe interceptorToStringValue
    }
    "InterceptorFailed" {
        val input = BallastDebuggerEventV3.InterceptorFailed(inputConnectionId, viewModelName, uuid, timestamp, interceptorType, interceptorToStringValue, stacktrace)
        val output = converter.mapEvent(input)
        output.shouldBeInstanceOf<BallastDebuggerEventV4.InterceptorFailed>()
        output.connectionId shouldBe inputConnectionId
        output.viewModelName shouldBe viewModelName
        output.uuid shouldBe uuid
        output.timestamp shouldBe timestamp
        output.interceptorType shouldBe interceptorType
        output.interceptorToStringValue shouldBe interceptorToStringValue
        output.stacktrace shouldBe stacktrace
    }
})
