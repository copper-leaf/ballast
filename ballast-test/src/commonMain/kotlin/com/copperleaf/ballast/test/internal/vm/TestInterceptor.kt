package com.copperleaf.ballast.test.internal.vm

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastNotification
import com.copperleaf.ballast.SideEffectScope
import com.copperleaf.ballast.test.TestResults

/**
 * An internal class used to keep the test framework
 */
internal class TestInterceptor<Inputs : Any, Events : Any, State : Any> :
    BallastInterceptor<TestViewModel.Inputs<Inputs>, Events, State> {

    private val acceptedInputs = mutableListOf<Inputs>()
    private val rejectedInputs = mutableListOf<Inputs>()
    private val droppedInputs = mutableListOf<Inputs>()
    private val successfulInputs = mutableListOf<Inputs>()
    private val cancelledInputs = mutableListOf<Inputs>()
    private val inputHandlerErrors = mutableListOf<Pair<Inputs, Throwable>>()

    private val events = mutableListOf<Events>()
    private val successfulEvents = mutableListOf<Events>()
    private val eventHandlerErrors = mutableListOf<Pair<Events, Throwable>>()

    private val states = mutableListOf<State>()

    private val sideEffects = mutableListOf<Pair<String, SideEffectScope.RestartState>>()
    private val completedSideEffects = mutableListOf<String>()
    private val sideEffectErrors = mutableListOf<Pair<String, Throwable>>()

    private val unhandledErrors = mutableListOf<Throwable>()

    private inline fun TestViewModel.Inputs<Inputs>.unwrap(block: (Inputs) -> Unit) {
        when (this) {
            is TestViewModel.Inputs.AwaitInput -> {
                block(this.normalInput)
            }
            is TestViewModel.Inputs.ProcessInput -> {
                block(this.normalInput)
            }
            is TestViewModel.Inputs.TestCompleted -> {
            }
        }
    }

    override suspend fun onNotify(notification: BallastNotification<TestViewModel.Inputs<Inputs>, Events, State>) {
        when (notification) {
            is BallastNotification.InputAccepted -> {
                notification.input.unwrap { acceptedInputs += it }
            }
            is BallastNotification.InputRejected -> {
                notification.input.unwrap { rejectedInputs += it }
            }
            is BallastNotification.InputDropped -> {
                notification.input.unwrap { droppedInputs += it }
            }
            is BallastNotification.InputHandledSuccessfully -> {
                notification.input.unwrap { successfulInputs += it }
            }
            is BallastNotification.InputCancelled -> {
                notification.input.unwrap { cancelledInputs += it }
            }
            is BallastNotification.InputHandlerError -> {
                notification.input.unwrap { inputHandlerErrors += it to notification.throwable }
            }

            is BallastNotification.EventEmitted -> {
                events += notification.event
            }
            is BallastNotification.EventHandledSuccessfully -> {
                successfulEvents += notification.event
            }
            is BallastNotification.EventHandlerError -> {
                eventHandlerErrors += notification.event to notification.throwable
            }

            is BallastNotification.StateChanged -> {
                states += notification.state
            }

            is BallastNotification.SideEffectStarted -> {
                sideEffects += notification.key to notification.restartState
            }
            is BallastNotification.SideEffectCompleted -> {
                completedSideEffects += notification.key
            }
            is BallastNotification.SideEffectError -> {
                sideEffectErrors += notification.key to notification.throwable
            }

            is BallastNotification.UnhandledError -> {
                unhandledErrors += notification.throwable
            }

            else -> {}
        }
    }

    internal suspend fun getResults(): TestResults<Inputs, Events, State> {
        // wait for the final notification to be received
        return TestResults(
            acceptedInputs = acceptedInputs.toList(),
            rejectedInputs = rejectedInputs.toList(),
            droppedInputs = droppedInputs.toList(),
            successfulInputs = successfulInputs.toList(),
            cancelledInputs = cancelledInputs.toList(),
            inputHandlerErrors = inputHandlerErrors.toList(),

            events = events.toList(),
            eventHandlerErrors = eventHandlerErrors.toList(),

            states = states.toList(),

            sideEffects = sideEffects.toList(),
            sideEffectErrors = sideEffectErrors.toList(),

            unhandledErrors = unhandledErrors.toList(),
        )
    }
}
