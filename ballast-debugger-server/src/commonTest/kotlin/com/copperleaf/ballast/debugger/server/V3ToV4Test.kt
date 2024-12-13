package com.copperleaf.ballast.debugger.server

import com.copperleaf.ballast.SideJobScope
import com.copperleaf.ballast.debugger.versions.v3.BallastDebuggerEventV3
import com.copperleaf.ballast.debugger.versions.v4.BallastDebuggerEventV4
import com.copperleaf.ballast.debugger.versions.v4.ClientModelConverterV3ToV4
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import java.time.Month
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class V3ToV4Test {
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

    @Test
    fun heartbeat() = runTest {
        val input = BallastDebuggerEventV3.Heartbeat(inputConnectionId, inputConnectionBallastVersion)
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV4.Heartbeat)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(inputConnectionBallastVersion, output.connectionBallastVersion)
    }

    @Test
    fun refreshViewModelStart() = runTest {
        val input = BallastDebuggerEventV3.RefreshViewModelStart(inputConnectionId, viewModelName)
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV4.RefreshViewModelStart)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
    }

    @Test
    fun refreshViewModelComplete() = runTest {
        val input = BallastDebuggerEventV3.RefreshViewModelComplete(inputConnectionId, viewModelName)
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV4.RefreshViewModelComplete)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
    }

    @Test
    fun viewModelStatusChanged() = runTest {
        val input = BallastDebuggerEventV3.ViewModelStatusChanged(
            inputConnectionId,
            viewModelName,
            viewModelType,
            uuid,
            timestamp,
            statusV3
        )
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV4.ViewModelStatusChanged)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(viewModelType, output.viewModelType)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(statusV4, output.status)
    }

    @Test
    fun inputQueued() = runTest {
        val input = BallastDebuggerEventV3.InputQueued(
            inputConnectionId,
            viewModelName,
            uuid,
            timestamp,
            inputType,
            inputToStringValue,
            inputContentType
        )
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV4.InputQueued)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(inputType, output.inputType)
        assertEquals<Any?>(inputToStringValue, output.serializedInput)
        assertEquals<Any?>(inputContentType, output.inputContentType)
    }

    @Test
    fun inputAccepted() = runTest {
        val input = BallastDebuggerEventV3.InputAccepted(
            inputConnectionId,
            viewModelName,
            uuid,
            timestamp,
            inputType,
            inputToStringValue,
            inputContentType
        )
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV4.InputAccepted)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(inputType, output.inputType)
        assertEquals<Any?>(inputToStringValue, output.serializedInput)
        assertEquals<Any?>(inputContentType, output.inputContentType)
    }

    @Test
    fun inputRejected() = runTest {
        val input = BallastDebuggerEventV3.InputRejected(
            inputConnectionId,
            viewModelName,
            uuid,
            timestamp,
            inputType,
            inputToStringValue,
            inputContentType
        )
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV4.InputRejected)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(inputType, output.inputType)
        assertEquals<Any?>(inputToStringValue, output.serializedInput)
        assertEquals<Any?>(inputContentType, output.inputContentType)
    }

    @Test
    fun inputDropped() = runTest {
        val input = BallastDebuggerEventV3.InputDropped(
            inputConnectionId,
            viewModelName,
            uuid,
            timestamp,
            inputType,
            inputToStringValue,
            inputContentType
        )
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV4.InputDropped)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(inputType, output.inputType)
        assertEquals<Any?>(inputToStringValue, output.serializedInput)
        assertEquals<Any?>(inputContentType, output.inputContentType)
    }

    @Test
    fun inputHandledSuccessfully() = runTest {
        val input = BallastDebuggerEventV3.InputHandledSuccessfully(
            inputConnectionId,
            viewModelName,
            uuid,
            timestamp,
            inputType,
            inputToStringValue,
            inputContentType
        )
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV4.InputHandledSuccessfully)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(inputType, output.inputType)
        assertEquals<Any?>(inputToStringValue, output.serializedInput)
        assertEquals<Any?>(inputContentType, output.inputContentType)
    }

    @Test
    fun inputCancelled() = runTest {
        val input = BallastDebuggerEventV3.InputCancelled(
            inputConnectionId,
            viewModelName,
            uuid,
            timestamp,
            inputType,
            inputToStringValue,
            inputContentType
        )
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV4.InputCancelled)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(inputType, output.inputType)
        assertEquals<Any?>(inputToStringValue, output.serializedInput)
        assertEquals<Any?>(inputContentType, output.inputContentType)
    }

    @Test
    fun inputHandlerError() = runTest {
        val input = BallastDebuggerEventV3.InputHandlerError(
            inputConnectionId,
            viewModelName,
            uuid,
            timestamp,
            inputType,
            inputToStringValue,
            inputContentType,
            stacktrace
        )
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV4.InputHandlerError)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(inputType, output.inputType)
        assertEquals<Any?>(inputToStringValue, output.serializedInput)
        assertEquals<Any?>(inputContentType, output.inputContentType)
        assertEquals<Any?>(stacktrace, output.stacktrace)
    }

    @Test
    fun eventQueued() = runTest {
        val input = BallastDebuggerEventV3.EventQueued(
            inputConnectionId,
            viewModelName,
            uuid,
            timestamp,
            eventType,
            eventToStringValue,
            eventContentType
        )
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV4.EventQueued)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(eventType, output.eventType)
        assertEquals<Any?>(eventToStringValue, output.serializedEvent)
        assertEquals<Any?>(eventContentType, output.eventContentType)
    }

    @Test
    fun eventEmitted() = runTest {
        val input = BallastDebuggerEventV3.EventEmitted(
            inputConnectionId,
            viewModelName,
            uuid,
            timestamp,
            eventType,
            eventToStringValue,
            eventContentType
        )
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV4.EventEmitted)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(eventType, output.eventType)
        assertEquals<Any?>(eventToStringValue, output.serializedEvent)
        assertEquals<Any?>(eventContentType, output.eventContentType)
    }

    @Test
    fun eventHandledSuccessfully() = runTest {
        val input = BallastDebuggerEventV3.EventHandledSuccessfully(
            inputConnectionId,
            viewModelName,
            uuid,
            timestamp,
            eventType,
            eventToStringValue,
            eventContentType
        )
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV4.EventHandledSuccessfully)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(eventType, output.eventType)
        assertEquals<Any?>(eventToStringValue, output.serializedEvent)
        assertEquals<Any?>(eventContentType, output.eventContentType)
    }

    @Test
    fun eventHandlerError() = runTest {
        val input = BallastDebuggerEventV3.EventHandlerError(
            inputConnectionId,
            viewModelName,
            uuid,
            timestamp,
            eventType,
            eventToStringValue,
            eventContentType,
            stacktrace
        )
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV4.EventHandlerError)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(eventType, output.eventType)
        assertEquals<Any?>(eventToStringValue, output.serializedEvent)
        assertEquals<Any?>(eventContentType, output.eventContentType)
        assertEquals<Any?>(stacktrace, output.stacktrace)
    }

    @Test
    fun eventProcessingStarted() = runTest {
        val input = BallastDebuggerEventV3.EventProcessingStarted(inputConnectionId, viewModelName, uuid, timestamp)
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV4.EventProcessingStarted)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
    }

    @Test
    fun eventProcessingStopped() = runTest {
        val input = BallastDebuggerEventV3.EventProcessingStopped(inputConnectionId, viewModelName, uuid, timestamp)
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV4.EventProcessingStopped)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
    }

    @Test
    fun stateChanged() = runTest {
        val input = BallastDebuggerEventV3.StateChanged(
            inputConnectionId,
            viewModelName,
            uuid,
            timestamp,
            stateType,
            stateToStringValue,
            stateContentType
        )
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV4.StateChanged)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(stateType, output.stateType)
        assertEquals<Any?>(stateToStringValue, output.serializedState)
        assertEquals<Any?>(stateContentType, output.stateContentType)
    }

    @Test
    fun sideJobQueued() = runTest {
        val input = BallastDebuggerEventV3.SideJobQueued(inputConnectionId, viewModelName, uuid, timestamp, key)
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV4.SideJobQueued)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(key, output.key)
    }

    @Test
    fun sideJobStarted() = runTest {
        val input =
            BallastDebuggerEventV3.SideJobStarted(inputConnectionId, viewModelName, uuid, timestamp, key, restartState)
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV4.SideJobStarted)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(key, output.key)
        assertEquals<Any?>(restartState, output.restartState)
    }

    @Test
    fun sideJobCompleted() = runTest {
        val input = BallastDebuggerEventV3.SideJobCompleted(
            inputConnectionId,
            viewModelName,
            uuid,
            timestamp,
            key,
            restartState
        )
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV4.SideJobCompleted)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(key, output.key)
        assertEquals<Any?>(restartState, output.restartState)
    }

    @Test
    fun sideJobCancelled() = runTest {
        val input = BallastDebuggerEventV3.SideJobCancelled(
            inputConnectionId,
            viewModelName,
            uuid,
            timestamp,
            key,
            restartState
        )
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV4.SideJobCancelled)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(key, output.key)
        assertEquals<Any?>(restartState, output.restartState)
    }

    @Test
    fun sideJobError() = runTest {
        val input = BallastDebuggerEventV3.SideJobError(
            inputConnectionId,
            viewModelName,
            uuid,
            timestamp,
            key,
            restartState,
            stacktrace
        )
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV4.SideJobError)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(key, output.key)
        assertEquals<Any?>(restartState, output.restartState)
        assertEquals<Any?>(stacktrace, output.stacktrace)
    }

    @Test
    fun unhandledError() = runTest {
        val input = BallastDebuggerEventV3.UnhandledError(inputConnectionId, viewModelName, uuid, timestamp, stacktrace)
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV4.UnhandledError)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(stacktrace, output.stacktrace)
    }

    @Test
    fun interceptorAttached() = runTest {
        val input = BallastDebuggerEventV3.InterceptorAttached(
            inputConnectionId,
            viewModelName,
            uuid,
            timestamp,
            interceptorType,
            interceptorToStringValue
        )
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV4.InterceptorAttached)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(interceptorType, output.interceptorType)
        assertEquals<Any?>(interceptorToStringValue, output.interceptorToStringValue)
    }

    @Test
    fun interceptorFailed() = runTest {
        val input = BallastDebuggerEventV3.InterceptorFailed(
            inputConnectionId,
            viewModelName,
            uuid,
            timestamp,
            interceptorType,
            interceptorToStringValue,
            stacktrace
        )
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV4.InterceptorFailed)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(interceptorType, output.interceptorType)
        assertEquals<Any?>(interceptorToStringValue, output.interceptorToStringValue)
        assertEquals<Any?>(stacktrace, output.stacktrace)
    }
}
