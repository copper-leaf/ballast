package com.copperleaf.ballast.debugger.server

import com.copperleaf.ballast.SideJobScope
import com.copperleaf.ballast.debugger.versions.v2.BallastDebuggerEventV2
import com.copperleaf.ballast.debugger.versions.v3.BallastDebuggerEventV3
import com.copperleaf.ballast.debugger.versions.v3.ClientModelConverterV2ToV3
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import java.time.Month
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class V2ToV3Test {
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

    @Test
    fun heartbeat() = runTest {
        val input = BallastDebuggerEventV2.Heartbeat(inputConnectionId, inputConnectionBallastVersion)
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV3.Heartbeat)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(inputConnectionBallastVersion, output.connectionBallastVersion)
    }

    @Test
    fun refreshViewModelStart() = runTest {
        val input = BallastDebuggerEventV2.RefreshViewModelStart(inputConnectionId, viewModelName)
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV3.RefreshViewModelStart)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
    }

    @Test
    fun refreshViewModelComplete() = runTest {
        val input = BallastDebuggerEventV2.RefreshViewModelComplete(inputConnectionId, viewModelName)
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV3.RefreshViewModelComplete)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
    }

    @Test
    fun viewModelStarted() = runTest {
        val input =
            BallastDebuggerEventV2.ViewModelStarted(inputConnectionId, viewModelName, viewModelType, uuid, timestamp)
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV3.ViewModelStatusChanged)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(viewModelType, output.viewModelType)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(BallastDebuggerEventV3.StatusV3.Running, output.status)
    }

    @Test
    fun viewModelCleared() = runTest {
        val input = BallastDebuggerEventV2.ViewModelCleared(inputConnectionId, viewModelName, uuid, timestamp)
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV3.ViewModelStatusChanged)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>("", output.viewModelType)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(BallastDebuggerEventV3.StatusV3.Cleared, output.status)
    }

    @Test
    fun inputQueued() = runTest {
        val input = BallastDebuggerEventV2.InputQueued(
            inputConnectionId,
            viewModelName,
            uuid,
            timestamp,
            inputType,
            inputToStringValue
        )
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV3.InputQueued)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(inputType, output.inputType)
        assertEquals<Any?>(inputToStringValue, output.serializedInput)
        assertEquals<Any?>("text/*", output.inputContentType)
    }

    @Test
    fun inputAccepted() = runTest {
        val input = BallastDebuggerEventV2.InputAccepted(
            inputConnectionId,
            viewModelName,
            uuid,
            timestamp,
            inputType,
            inputToStringValue
        )
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV3.InputAccepted)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(inputType, output.inputType)
        assertEquals<Any?>(inputToStringValue, output.serializedInput)
        assertEquals<Any?>("text/*", output.inputContentType)
    }

    @Test
    fun inputRejected() = runTest {
        val input = BallastDebuggerEventV2.InputRejected(
            inputConnectionId,
            viewModelName,
            uuid,
            timestamp,
            inputType,
            inputToStringValue
        )
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV3.InputRejected)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(inputType, output.inputType)
        assertEquals<Any?>(inputToStringValue, output.serializedInput)
        assertEquals<Any?>("text/*", output.inputContentType)
    }

    @Test
    fun inputDropped() = runTest {
        val input = BallastDebuggerEventV2.InputDropped(
            inputConnectionId,
            viewModelName,
            uuid,
            timestamp,
            inputType,
            inputToStringValue
        )
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV3.InputDropped)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(inputType, output.inputType)
        assertEquals<Any?>(inputToStringValue, output.serializedInput)
        assertEquals<Any?>("text/*", output.inputContentType)
    }

    @Test
    fun inputHandledSuccessfully() = runTest {
        val input = BallastDebuggerEventV2.InputHandledSuccessfully(
            inputConnectionId,
            viewModelName,
            uuid,
            timestamp,
            inputType,
            inputToStringValue
        )
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV3.InputHandledSuccessfully)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(inputType, output.inputType)
        assertEquals<Any?>(inputToStringValue, output.serializedInput)
        assertEquals<Any?>("text/*", output.inputContentType)
    }

    @Test
    fun inputCancelled() = runTest {
        val input = BallastDebuggerEventV2.InputCancelled(
            inputConnectionId,
            viewModelName,
            uuid,
            timestamp,
            inputType,
            inputToStringValue
        )
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV3.InputCancelled)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(inputType, output.inputType)
        assertEquals<Any?>(inputToStringValue, output.serializedInput)
        assertEquals<Any?>("text/*", output.inputContentType)
    }

    @Test
    fun inputHandlerError() = runTest {
        val input = BallastDebuggerEventV2.InputHandlerError(
            inputConnectionId,
            viewModelName,
            uuid,
            timestamp,
            inputType,
            inputToStringValue,
            stacktrace
        )
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV3.InputHandlerError)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(inputType, output.inputType)
        assertEquals<Any?>(inputToStringValue, output.serializedInput)
        assertEquals<Any?>("text/*", output.inputContentType)
        assertEquals<Any?>(stacktrace, output.stacktrace)
    }

    @Test
    fun eventQueued() = runTest {
        val input = BallastDebuggerEventV2.EventQueued(
            inputConnectionId,
            viewModelName,
            uuid,
            timestamp,
            eventType,
            eventToStringValue
        )
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV3.EventQueued)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(eventType, output.eventType)
        assertEquals<Any?>(eventToStringValue, output.serializedEvent)
        assertEquals<Any?>("text/*", output.eventContentType)
    }

    @Test
    fun eventEmitted() = runTest {
        val input = BallastDebuggerEventV2.EventEmitted(
            inputConnectionId,
            viewModelName,
            uuid,
            timestamp,
            eventType,
            eventToStringValue
        )
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV3.EventEmitted)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(eventType, output.eventType)
        assertEquals<Any?>(eventToStringValue, output.serializedEvent)
        assertEquals<Any?>("text/*", output.eventContentType)
    }

    @Test
    fun eventHandledSuccessfully() = runTest {
        val input = BallastDebuggerEventV2.EventHandledSuccessfully(
            inputConnectionId,
            viewModelName,
            uuid,
            timestamp,
            eventType,
            eventToStringValue
        )
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV3.EventHandledSuccessfully)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(eventType, output.eventType)
        assertEquals<Any?>(eventToStringValue, output.serializedEvent)
        assertEquals<Any?>("text/*", output.eventContentType)
    }

    @Test
    fun eventHandlerError() = runTest {
        val input = BallastDebuggerEventV2.EventHandlerError(
            inputConnectionId,
            viewModelName,
            uuid,
            timestamp,
            eventType,
            eventToStringValue,
            stacktrace
        )
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV3.EventHandlerError)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(eventType, output.eventType)
        assertEquals<Any?>(eventToStringValue, output.serializedEvent)
        assertEquals<Any?>("text/*", output.eventContentType)
        assertEquals<Any?>(stacktrace, output.stacktrace)
    }

    @Test
    fun eventProcessingStarted() = runTest {
        val input = BallastDebuggerEventV2.EventProcessingStarted(inputConnectionId, viewModelName, uuid, timestamp)
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV3.EventProcessingStarted)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
    }

    @Test
    fun eventProcessingStopped() = runTest {
        val input = BallastDebuggerEventV2.EventProcessingStopped(inputConnectionId, viewModelName, uuid, timestamp)
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV3.EventProcessingStopped)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
    }

    @Test
    fun stateChanged() = runTest {
        val input = BallastDebuggerEventV2.StateChanged(
            inputConnectionId,
            viewModelName,
            uuid,
            timestamp,
            stateType,
            stateToStringValue
        )
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV3.StateChanged)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(stateType, output.stateType)
        assertEquals<Any?>(stateToStringValue, output.serializedState)
        assertEquals<Any?>("text/*", output.stateContentType)
    }

    @Test
    fun sideJobQueued() = runTest {
        val input = BallastDebuggerEventV2.SideJobQueued(inputConnectionId, viewModelName, uuid, timestamp, key)
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV3.SideJobQueued)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(key, output.key)
    }

    @Test
    fun sideJobStarted() = runTest {
        val input =
            BallastDebuggerEventV2.SideJobStarted(inputConnectionId, viewModelName, uuid, timestamp, key, restartState)
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV3.SideJobStarted)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(key, output.key)
        assertEquals<Any?>(restartState, output.restartState)
    }

    @Test
    fun sideJobCompleted() = runTest {
        val input = BallastDebuggerEventV2.SideJobCompleted(
            inputConnectionId,
            viewModelName,
            uuid,
            timestamp,
            key,
            restartState
        )
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV3.SideJobCompleted)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(key, output.key)
        assertEquals<Any?>(restartState, output.restartState)
    }

    @Test
    fun sideJobCancelled() = runTest {
        val input = BallastDebuggerEventV2.SideJobCancelled(
            inputConnectionId,
            viewModelName,
            uuid,
            timestamp,
            key,
            restartState
        )
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV3.SideJobCancelled)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(key, output.key)
        assertEquals<Any?>(restartState, output.restartState)
    }

    @Test
    fun sideJobError() = runTest {
        val input = BallastDebuggerEventV2.SideJobError(
            inputConnectionId,
            viewModelName,
            uuid,
            timestamp,
            key,
            restartState,
            stacktrace
        )
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV3.SideJobError)
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
        val input = BallastDebuggerEventV2.UnhandledError(inputConnectionId, viewModelName, uuid, timestamp, stacktrace)
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV3.UnhandledError)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(stacktrace, output.stacktrace)
    }
}
