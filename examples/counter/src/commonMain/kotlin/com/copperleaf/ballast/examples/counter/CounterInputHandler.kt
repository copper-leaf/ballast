package com.copperleaf.ballast.examples.counter

import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputHandlerScope

class CounterInputHandler : InputHandler<
        CounterContract.Inputs,
        CounterContract.Events,
        CounterContract.State> {
    override suspend fun InputHandlerScope<
            CounterContract.Inputs,
            CounterContract.Events,
            CounterContract.State>.handleInput(
        input: CounterContract.Inputs
    ) = when (input) {
        is CounterContract.Inputs.Increment -> {
            val updatedState = updateStateAndGet { it.copy(count = it.count + input.amount) }

            if (updatedState.count == 10) {
                postEvent(CounterContract.Events.OnTenReached)
            } else {
                noOp()
            }
        }

        is CounterContract.Inputs.Decrement -> {
            updateState { it.copy(count = it.count - input.amount) }
        }

        is CounterContract.Inputs.Reset -> {
            updateState { it.copy(count = 0) }
        }
    }
}
