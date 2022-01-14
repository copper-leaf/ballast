package com.copperleaf.ballast.test.internal.vm

import com.copperleaf.ballast.InputFilter

internal class TestInputFilter<Inputs : Any, Events : Any, State : Any>(
    private val filterDelegate: InputFilter<Inputs, Events, State>
) : InputFilter<TestViewModel.Inputs<Inputs>, Events, State> {
    override fun filterInput(
        state: State,
        input: TestViewModel.Inputs<Inputs>
    ): InputFilter.Result = when (input) {
        is TestViewModel.Inputs.ProcessInput -> {
            val result = filterDelegate.filterInput(state, input.normalInput)

            if (result == InputFilter.Result.Reject) {
                input.processingStarted.complete(Unit)
            }

            result
        }
        is TestViewModel.Inputs.AwaitInput -> {
            val result = filterDelegate.filterInput(state, input.normalInput)

            if (result == InputFilter.Result.Reject) {
                input.processingFinished.complete(Unit)
            }

            result
        }
        is TestViewModel.Inputs.TestCompleted -> InputFilter.Result.Accept
    }
}
