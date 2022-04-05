package com.copperleaf.ballast.impl

import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputHandlerScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.yield

class TestInputHandler : InputHandler<
    TestContract.Inputs,
    TestContract.Events,
    TestContract.State> {
    override suspend fun InputHandlerScope<
        TestContract.Inputs,
        TestContract.Events,
        TestContract.State>.handleInput(
        input: TestContract.Inputs
    ) = when (input) {
        is TestContract.Inputs.FilteredValue -> {
            updateState { it.copy(acceptedFilteredValue = true) }
        }
        is TestContract.Inputs.ThrowErrorDuringHandling -> {
            delay(1000)
            error("error thrown during handling")
        }
        is TestContract.Inputs.UpdateStringValue -> {
            delay(1000)
            updateState { it.copy(stringValue = input.stringValue) }
        }
        is TestContract.Inputs.Increment -> {
            delay(1000)
            updateState { it.copy(intValue = it.intValue + 1) }
        }
        is TestContract.Inputs.Decrement -> {
            delay(1000)
            updateState { it.copy(intValue = it.intValue - 1) }
        }
        is TestContract.Inputs.IncrementWithRollback -> {
            updateState { it.copy(intValue = it.intValue + 1) }
            delay(1000)
        }
        is TestContract.Inputs.MultipleStateUpdates -> {
            updateState { it.copy(intValue = it.intValue + 1) }
            yield()
            updateState { it.copy(intValue = it.intValue + 1) }
            delay(1000)
        }
        is TestContract.Inputs.EventEmitted -> {
            postEvent(TestContract.Events.Notification)
        }
        is TestContract.Inputs.SideJobStartedNoInputOverride -> {
            sideJob("SideJobStartedNoInputOverride") {
                delay(1500)

                flowOf("one", "two", "three")
                    .onEach { delay(1500) }
                    .map { TestContract.Inputs.UpdateStringValue(it) }
                    .collect { postInput(it) }

                delay(1500)
            }
        }
        is TestContract.Inputs.SideJobStartedWithInputOverride -> {
            sideJob("SideJobStartedWithInputOverride") {
                delay(1500)

                flowOf("one", "two", "three")
                    .map { TestContract.Inputs.UpdateStringValue(it) }
                    .collect { postInput(it) }

                delay(1500)
            }
        }
        is TestContract.Inputs.MultipleSideJobs -> {
            sideJob("one") {
                delay(1500)
                postInput(TestContract.Inputs.Increment)
                delay(1500)
                postInput(TestContract.Inputs.Increment)
                delay(1500)
            }
            sideJob("two") {
                delay(1500)
                postInput(TestContract.Inputs.Increment)
                delay(1500)
                postInput(TestContract.Inputs.Increment)
                delay(1500)
            }
        }
        is TestContract.Inputs.SideJobsNotAtEnd -> {
            sideJob("SideJobsNotAtEnd") {
                delay(1500)
                postInput(TestContract.Inputs.Increment)
                delay(1500)
                postInput(TestContract.Inputs.Increment)
                delay(1500)
            }
            getCurrentState()
            Unit
        }
    }
}
