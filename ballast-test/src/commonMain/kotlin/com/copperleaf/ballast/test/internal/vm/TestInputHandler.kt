package com.copperleaf.ballast.test.internal.vm

import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputHandlerScope

internal class TestInputHandler<Inputs : Any, Events : Any, State : Any>(
    private val inputHandlerDelegate: InputHandler<Inputs, Events, State>,
) : InputHandler<TestViewModel.Inputs<Inputs>, Events, State> {

    override suspend fun InputHandlerScope<TestViewModel.Inputs<Inputs>, Events, State>.handleInput(
        input: TestViewModel.Inputs<Inputs>
    ) {
        try {
            doHandleInput(input)
        } catch (t: Throwable) {
            recoverFromError(input)
            throw t
        }
    }

    private suspend fun InputHandlerScope<TestViewModel.Inputs<Inputs>, Events, State>.doHandleInput(
        input: TestViewModel.Inputs<Inputs>
    ) {
        when (input) {
            is TestViewModel.Inputs.ProcessInput<Inputs> -> {
                logger.debug("            before handling normal input")
                input.processingStarted.complete(Unit)

                val scopeDelegate = TestInputHandlerScope(this)
                with(inputHandlerDelegate) {
                    scopeDelegate.handleInput(input.normalInput)
                }
                logger.debug("            after handling normal input")
                Unit
            }
            is TestViewModel.Inputs.AwaitInput<Inputs> -> {
                logger.debug("            before handling normal input")
                val scopeDelegate = TestInputHandlerScope(this)
                with(inputHandlerDelegate) {
                    scopeDelegate.handleInput(input.normalInput)
                }
                input.processingFinished.complete(Unit)
                logger.debug("            after handling normal input")
                Unit
            }
            is TestViewModel.Inputs.TestCompleted<Inputs> -> {
                logger.debug("            before completing test")
                input.processingFinished.complete(Unit)
                logger.debug("            after completing test")
                noOp()
                Unit
            }
        }
    }

    private suspend fun InputHandlerScope<TestViewModel.Inputs<Inputs>, Events, State>.recoverFromError(
        input: TestViewModel.Inputs<Inputs>
    ) {
        when (input) {
            is TestViewModel.Inputs.ProcessInput<Inputs> -> {
                input.processingStarted.complete(Unit)
                Unit
            }
            is TestViewModel.Inputs.AwaitInput<Inputs> -> {
                input.processingFinished.complete(Unit)
                Unit
            }
            is TestViewModel.Inputs.TestCompleted<Inputs> -> {
                input.processingFinished.complete(Unit)
                Unit
            }
        }
    }
}
