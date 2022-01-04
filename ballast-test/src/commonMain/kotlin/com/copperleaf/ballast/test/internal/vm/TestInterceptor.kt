package com.copperleaf.ballast.test.internal.vm

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.test.TestResults

internal class TestInterceptor<Inputs : Any, Events : Any, State : Any> :
    BallastInterceptor<TestViewModel.Inputs<Inputs, State>, Events, State> {

// Inputs
// ---------------------------------------------------------------------------------------------------------------------

    private val acceptedInputs = mutableListOf<Inputs>()
    override suspend fun onInputAccepted(input: TestViewModel.Inputs<Inputs, State>) {
        when (input) {
            is TestViewModel.Inputs.AwaitInput -> {
                acceptedInputs += input.normalInput
            }
            is TestViewModel.Inputs.ProcessInput -> {
                acceptedInputs += input.normalInput
            }
            is TestViewModel.Inputs.TestCompleted -> {
            }
        }
    }

    private val rejectedInputs = mutableListOf<Inputs>()
    override suspend fun onInputRejected(input: TestViewModel.Inputs<Inputs, State>) {
        when (input) {
            is TestViewModel.Inputs.AwaitInput -> {
                rejectedInputs += input.normalInput
            }
            is TestViewModel.Inputs.ProcessInput -> {
                rejectedInputs += input.normalInput
            }
            is TestViewModel.Inputs.TestCompleted -> {
            }
        }
    }

    private val droppedInputs = mutableListOf<Inputs>()
    override fun onInputDropped(input: TestViewModel.Inputs<Inputs, State>) {
        when (input) {
            is TestViewModel.Inputs.AwaitInput -> {
                droppedInputs += input.normalInput
            }
            is TestViewModel.Inputs.ProcessInput -> {
                droppedInputs += input.normalInput
            }
            is TestViewModel.Inputs.TestCompleted -> {
            }
        }
    }

    private val successfulInputs = mutableListOf<Inputs>()
    override suspend fun onInputHandledSuccessfully(input: TestViewModel.Inputs<Inputs, State>) {
        when (input) {
            is TestViewModel.Inputs.AwaitInput -> {
                successfulInputs += input.normalInput
            }
            is TestViewModel.Inputs.ProcessInput -> {
                successfulInputs += input.normalInput
            }
            is TestViewModel.Inputs.TestCompleted -> {
            }
        }
    }

    private val cancelledInputs = mutableListOf<Inputs>()
    override suspend fun onInputCancelled(input: TestViewModel.Inputs<Inputs, State>) {
        when (input) {
            is TestViewModel.Inputs.AwaitInput -> {
                cancelledInputs += input.normalInput
            }
            is TestViewModel.Inputs.ProcessInput -> {
                cancelledInputs += input.normalInput
            }
            is TestViewModel.Inputs.TestCompleted -> {
            }
        }
    }

    private val inputHandlerErrors = mutableListOf<Pair<Inputs, Throwable>>()
    override suspend fun onInputHandlerError(input: TestViewModel.Inputs<Inputs, State>, exception: Throwable) {
        when (input) {
            is TestViewModel.Inputs.AwaitInput -> {
                inputHandlerErrors += input.normalInput to exception
            }
            is TestViewModel.Inputs.ProcessInput -> {
                inputHandlerErrors += input.normalInput to exception
            }
            is TestViewModel.Inputs.TestCompleted -> {
            }
        }
    }

// Events
// ---------------------------------------------------------------------------------------------------------------------

    private val events = mutableListOf<Events>()
    override suspend fun onEventEmitted(event: Events) {
        events += event
    }

    private val eventHandlerErrors = mutableListOf<Pair<Events, Throwable>>()
    override suspend fun onEventHandlerError(event: Events, exception: Throwable) {
        eventHandlerErrors += event to exception
    }

// States
// ---------------------------------------------------------------------------------------------------------------------

    private val states = mutableListOf<State>()
    override suspend fun onStateEmitted(state: State) {
        states += state
    }

// Other
// ---------------------------------------------------------------------------------------------------------------------

    private val unhandledErrors = mutableListOf<Throwable>()
    override fun onUnhandledError(exception: Throwable) {
        unhandledErrors += exception
    }

    internal fun getResults(): TestResults<Inputs, Events, State> {
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

            unhandledErrors = unhandledErrors.toList(),
        )
    }
}
