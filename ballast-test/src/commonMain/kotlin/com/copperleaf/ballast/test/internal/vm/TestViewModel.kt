package com.copperleaf.ballast.test.internal.vm

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastViewModel
import com.copperleaf.ballast.InputFilter
import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputStrategy
import com.copperleaf.ballast.core.DefaultViewModelConfiguration
import com.copperleaf.ballast.core.LoggingInterceptor
import com.copperleaf.ballast.internal.BallastViewModelImpl
import kotlinx.coroutines.CompletableDeferred

internal class TestViewModel<Inputs : Any, Events : Any, State : Any> internal constructor(
    internal val logger: (String) -> Unit,
    internal val testInterceptor: TestInterceptor<Inputs, Events, State>,
    internal val otherInterceptors: List<BallastInterceptor<TestViewModel.Inputs<Inputs>, Events, State>>,
    internal val initialState: State,
    internal val inputHandler: InputHandler<Inputs, Events, State>,
    internal val filter: InputFilter<TestViewModel.Inputs<Inputs>, Events, State>?,
    internal val inputStrategy: InputStrategy,
    name: String,
    internal val impl: BallastViewModelImpl<TestViewModel.Inputs<Inputs>, Events, State> = BallastViewModelImpl(
        DefaultViewModelConfiguration.Builder(name)
            .apply {
                this.initialState = initialState
                this.inputHandler = TestInputHandler(logger, inputHandler)

                this += otherInterceptors
                this += LoggingInterceptor(
                    logMessage = { logger(it) },
                    logError = { logger(it.message ?: "") },
                )
                this += testInterceptor

                this.filter = filter
                this.inputStrategy = inputStrategy
            }
            .build()
    ),
) : BallastViewModel<TestViewModel.Inputs<Inputs>, Events, State> by impl {

    override val type: String = "TestViewModel"

    sealed class Inputs<BaseInputs : Any> {
        data class ProcessInput<BaseInputs : Any>(
            val normalInput: BaseInputs,
            val processingStarted: CompletableDeferred<Unit>,
        ) : Inputs<BaseInputs>()

        data class AwaitInput<BaseInputs : Any>(
            val normalInput: BaseInputs,
            val processingFinished: CompletableDeferred<Unit>,
        ) : Inputs<BaseInputs>()

        data class TestCompleted<BaseInputs : Any>(
            val processingFinished: CompletableDeferred<Unit>
        ) : Inputs<BaseInputs>()
    }
}
