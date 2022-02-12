package com.copperleaf.ballast.test.internal.vm

import com.copperleaf.ballast.BallastViewModel
import com.copperleaf.ballast.InputFilter
import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputStrategy
import com.copperleaf.ballast.core.DefaultViewModelConfiguration
import com.copperleaf.ballast.core.DelegatingInterceptor
import com.copperleaf.ballast.core.LoggingInterceptor
import com.copperleaf.ballast.internal.BallastViewModelImpl
import kotlinx.coroutines.CompletableDeferred

internal class TestViewModel<Inputs : Any, Events : Any, State : Any> internal constructor(
    internal val logger: (String) -> Unit,
    internal val interceptor: TestInterceptor<Inputs, Events, State>,
    internal val initialState: State,
    internal val inputHandler: InputHandler<Inputs, Events, State>,
    internal val filter: InputFilter<TestViewModel.Inputs<Inputs>, Events, State>?,
    internal val inputStrategy: InputStrategy,
    internal val impl: BallastViewModelImpl<TestViewModel.Inputs<Inputs>, Events, State> = BallastViewModelImpl(
        DefaultViewModelConfiguration(
            initialState = initialState,
            inputHandler = TestInputHandler(logger, inputHandler),
            interceptor = DelegatingInterceptor(
                interceptor,
                LoggingInterceptor(
                    logMessage = { logger(it) },
                    logError = { logger(it.message ?: "") },
                )
            ),
            filter = filter,
            inputStrategy = inputStrategy,
        ),
    ),
) : BallastViewModel<TestViewModel.Inputs<Inputs>, Events, State> by impl {

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
