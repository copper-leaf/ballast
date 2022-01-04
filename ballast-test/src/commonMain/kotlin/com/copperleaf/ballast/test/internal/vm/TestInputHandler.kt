package com.copperleaf.ballast.test.internal.vm

import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputHandlerScope

internal class TestInputHandler<Inputs : Any, Events : Any, State : Any>(
    private val logger: (String) -> Unit,
    private val inputHandlerDelegate: InputHandler<Inputs, Events, State>,
) : InputHandler<TestViewModel.Inputs<Inputs, State>, Events, State> {

    override suspend fun InputHandlerScope<TestViewModel.Inputs<Inputs, State>, Events, State>.handleInput(
        input: TestViewModel.Inputs<Inputs, State>
    ) {
        try {
            doHandleInput(input)
        } catch (t: Throwable) {
            recoverFromError(input)
            throw t
        }
    }

    private suspend fun InputHandlerScope<TestViewModel.Inputs<Inputs, State>, Events, State>.doHandleInput(
        input: TestViewModel.Inputs<Inputs, State>
    ) {
        when (input) {
            is TestViewModel.Inputs.ProcessInput<Inputs, State> -> {
                logger("            before handling normal input")
                input.processingStarted.complete(Unit)

                val scopeDelegate = TestInputHandlerScope(this)
                with(inputHandlerDelegate) {
                    scopeDelegate.handleInput(input.normalInput)
                }
                logger("            after handling normal input")
                Unit
            }
            is TestViewModel.Inputs.AwaitInput<Inputs, State> -> {
                logger("            before handling normal input")
                val scopeDelegate = TestInputHandlerScope(this)
                with(inputHandlerDelegate) {
                    scopeDelegate.handleInput(input.normalInput)
                }
                input.processingFinished.complete(getCurrentState())
                logger("            after handling normal input")
                Unit
            }
            is TestViewModel.Inputs.TestCompleted<Inputs, State> -> {
                logger("            before completing test")
                input.processingFinished.complete(getCurrentState())
                logger("            after completing test")
                noOp()
                Unit
            }
        }
    }

    private suspend fun InputHandlerScope<TestViewModel.Inputs<Inputs, State>, Events, State>.recoverFromError(
        input: TestViewModel.Inputs<Inputs, State>
    ) {
        when (input) {
            is TestViewModel.Inputs.ProcessInput<Inputs, State> -> {
                input.processingStarted.complete(Unit)
                Unit
            }
            is TestViewModel.Inputs.AwaitInput<Inputs, State> -> {
                input.processingFinished.complete(getCurrentState())
                Unit
            }
            is TestViewModel.Inputs.TestCompleted<Inputs, State> -> {
                input.processingFinished.complete(getCurrentState())
                Unit
            }
        }
    }
}
