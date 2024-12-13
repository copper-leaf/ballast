package com.copperleaf.ballast.debugger.server

import com.copperleaf.ballast.SideJobScope
import com.copperleaf.ballast.debugger.versions.v1.BallastDebuggerEventV1
import com.copperleaf.ballast.debugger.versions.v2.BallastDebuggerEventV2
import com.copperleaf.ballast.debugger.versions.v2.ClientModelConverterV1ToV2
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import java.time.Month
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class V1ToV2Test {
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

    @Test
    fun heartbeat() = runTest {
        val input = BallastDebuggerEventV1.Heartbeat(inputConnectionId, inputConnectionBallastVersion)
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV2.Heartbeat)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(inputConnectionBallastVersion, output.connectionBallastVersion)
    }

    @Test
    fun refreshViewModelStart() = runTest {
        val input = BallastDebuggerEventV1.RefreshViewModelStart(inputConnectionId, viewModelName)
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV2.RefreshViewModelStart)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
    }

    @Test
    fun refreshViewModelComplete() = runTest {
        val input = BallastDebuggerEventV1.RefreshViewModelComplete(inputConnectionId, viewModelName)
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV2.RefreshViewModelComplete)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
    }

    @Test
    fun viewModelStarted() = runTest {
        val input =
            BallastDebuggerEventV1.ViewModelStarted(inputConnectionId, viewModelName, viewModelType, uuid, timestamp)
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV2.ViewModelStarted)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(viewModelType, output.viewModelType)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
    }

    @Test
    fun viewModelCleared() = runTest {
        val input = BallastDebuggerEventV1.ViewModelCleared(inputConnectionId, viewModelName, uuid, timestamp)
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV2.ViewModelCleared)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
    }

    @Test
    fun inputQueued() = runTest {
        val input = BallastDebuggerEventV1.InputQueued(
            inputConnectionId,
            viewModelName,
            uuid,
            timestamp,
            inputType,
            inputToStringValue
        )
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV2.InputQueued)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(inputType, output.inputType)
        assertEquals<Any?>(inputToStringValue, output.inputToStringValue)
    }

    @Test
    fun inputAccepted() = runTest {
        val input = BallastDebuggerEventV1.InputAccepted(
            inputConnectionId,
            viewModelName,
            uuid,
            timestamp,
            inputType,
            inputToStringValue
        )
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV2.InputAccepted)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(inputType, output.inputType)
        assertEquals<Any?>(inputToStringValue, output.inputToStringValue)
    }

    @Test
    fun inputRejected() = runTest {
        val input = BallastDebuggerEventV1.InputRejected(
            inputConnectionId,
            viewModelName,
            uuid,
            timestamp,
            inputType,
            inputToStringValue
        )
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV2.InputRejected)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(inputType, output.inputType)
        assertEquals<Any?>(inputToStringValue, output.inputToStringValue)
    }

    @Test
    fun inputDropped() = runTest {
        val input = BallastDebuggerEventV1.InputDropped(
            inputConnectionId,
            viewModelName,
            uuid,
            timestamp,
            inputType,
            inputToStringValue
        )
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV2.InputDropped)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(inputType, output.inputType)
        assertEquals<Any?>(inputToStringValue, output.inputToStringValue)
    }

    @Test
    fun inputHandledSuccessfully() = runTest {
        val input = BallastDebuggerEventV1.InputHandledSuccessfully(
            inputConnectionId,
            viewModelName,
            uuid,
            timestamp,
            inputType,
            inputToStringValue
        )
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV2.InputHandledSuccessfully)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(inputType, output.inputType)
        assertEquals<Any?>(inputToStringValue, output.inputToStringValue)
    }

    @Test
    fun inputCancelled() = runTest {
        val input = BallastDebuggerEventV1.InputCancelled(
            inputConnectionId,
            viewModelName,
            uuid,
            timestamp,
            inputType,
            inputToStringValue
        )
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV2.InputCancelled)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(inputType, output.inputType)
        assertEquals<Any?>(inputToStringValue, output.inputToStringValue)
    }

    @Test
    fun inputHandlerError() = runTest {
        val input = BallastDebuggerEventV1.InputHandlerError(
            inputConnectionId,
            viewModelName,
            uuid,
            timestamp,
            inputType,
            inputToStringValue,
            stacktrace
        )
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV2.InputHandlerError)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(inputType, output.inputType)
        assertEquals<Any?>(inputToStringValue, output.inputToStringValue)
        assertEquals<Any?>(stacktrace, output.stacktrace)
    }

    @Test
    fun eventQueued() = runTest {
        val input = BallastDebuggerEventV1.EventQueued(
            inputConnectionId,
            viewModelName,
            uuid,
            timestamp,
            eventType,
            eventToStringValue
        )
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV2.EventQueued)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(eventType, output.eventType)
        assertEquals<Any?>(eventToStringValue, output.eventToStringValue)
    }

    @Test
    fun eventEmitted() = runTest {
        val input = BallastDebuggerEventV1.EventEmitted(
            inputConnectionId,
            viewModelName,
            uuid,
            timestamp,
            eventType,
            eventToStringValue
        )
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV2.EventEmitted)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(eventType, output.eventType)
        assertEquals<Any?>(eventToStringValue, output.eventToStringValue)
    }

    @Test
    fun eventHandledSuccessfully() = runTest {
        val input = BallastDebuggerEventV1.EventHandledSuccessfully(
            inputConnectionId,
            viewModelName,
            uuid,
            timestamp,
            eventType,
            eventToStringValue
        )
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV2.EventHandledSuccessfully)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(eventType, output.eventType)
        assertEquals<Any?>(eventToStringValue, output.eventToStringValue)
    }

    @Test
    fun eventHandlerError() = runTest {
        val input = BallastDebuggerEventV1.EventHandlerError(
            inputConnectionId,
            viewModelName,
            uuid,
            timestamp,
            eventType,
            eventToStringValue,
            stacktrace
        )
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV2.EventHandlerError)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(eventType, output.eventType)
        assertEquals<Any?>(eventToStringValue, output.eventToStringValue)
        assertEquals<Any?>(stacktrace, output.stacktrace)
    }

    @Test
    fun eventProcessingStarted() = runTest {
        val input = BallastDebuggerEventV1.EventProcessingStarted(inputConnectionId, viewModelName, uuid, timestamp)
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV2.EventProcessingStarted)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
    }

    @Test
    fun eventProcessingStopped() = runTest {
        val input = BallastDebuggerEventV1.EventProcessingStopped(inputConnectionId, viewModelName, uuid, timestamp)
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV2.EventProcessingStopped)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
    }

    @Test
    fun stateChanged() = runTest {
        val input = BallastDebuggerEventV1.StateChanged(
            inputConnectionId,
            viewModelName,
            uuid,
            timestamp,
            stateType,
            stateToStringValue
        )
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV2.StateChanged)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(stateType, output.stateType)
        assertEquals<Any?>(stateToStringValue, output.stateToStringValue)
    }

    @Test
    fun sideJobQueued() = runTest {
        val input = BallastDebuggerEventV1.SideJobQueued(inputConnectionId, viewModelName, uuid, timestamp, key)
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV2.SideJobQueued)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(key, output.key)
    }

    @Test
    fun sideJobStarted() = runTest {
        val input =
            BallastDebuggerEventV1.SideJobStarted(inputConnectionId, viewModelName, uuid, timestamp, key, restartState)
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV2.SideJobStarted)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(key, output.key)
        assertEquals<Any?>(restartState, output.restartState)
    }

    @Test
    fun sideJobCompleted() = runTest {
        val input = BallastDebuggerEventV1.SideJobCompleted(
            inputConnectionId,
            viewModelName,
            uuid,
            timestamp,
            key,
            restartState
        )
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV2.SideJobCompleted)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(key, output.key)
        assertEquals<Any?>(restartState, output.restartState)
    }

    @Test
    fun sideJobCancelled() = runTest {
        val input = BallastDebuggerEventV1.SideJobCancelled(
            inputConnectionId,
            viewModelName,
            uuid,
            timestamp,
            key,
            restartState
        )
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV2.SideJobCancelled)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(key, output.key)
        assertEquals<Any?>(restartState, output.restartState)
    }

    @Test
    fun sideJobError() = runTest {
        val input = BallastDebuggerEventV1.SideJobError(
            inputConnectionId,
            viewModelName,
            uuid,
            timestamp,
            key,
            restartState,
            stacktrace
        )
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV2.SideJobError)
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
        val input = BallastDebuggerEventV1.UnhandledError(inputConnectionId, viewModelName, uuid, timestamp, stacktrace)
        val output = converter.mapEvent(input)
        assertTrue(output is BallastDebuggerEventV2.UnhandledError)
        assertEquals<Any?>(inputConnectionId, output.connectionId)
        assertEquals<Any?>(viewModelName, output.viewModelName)
        assertEquals<Any?>(uuid, output.uuid)
        assertEquals<Any?>(timestamp, output.timestamp)
        assertEquals<Any?>(stacktrace, output.stacktrace)
    }
}
