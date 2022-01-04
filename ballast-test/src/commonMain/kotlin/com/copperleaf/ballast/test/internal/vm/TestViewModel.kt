package com.copperleaf.ballast.test.internal.vm

import com.copperleaf.ballast.BallastViewModel
import com.copperleaf.ballast.InputFilter
import com.copperleaf.ballast.InputHandler
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
    internal val filter: InputFilter<TestViewModel.Inputs<Inputs, State>, Events, State>?,
    internal val impl: BallastViewModelImpl<TestViewModel.Inputs<Inputs, State>, Events, State> = BallastViewModelImpl(
        initialState,
        DefaultViewModelConfiguration(
            inputHandler = TestInputHandler(logger, inputHandler),
            interceptor = DelegatingInterceptor(
                interceptor,
                LoggingInterceptor(
                    logMessage = { logger(it) },
                    logError = { logger(it.message ?: "") },
                )
            ),
            filter = filter,
        ),
    ),
) : BallastViewModel<TestViewModel.Inputs<Inputs, State>, Events, State> by impl {

    sealed class Inputs<BaseInputs : Any, State : Any> {
        data class ProcessInput<BaseInputs : Any, State : Any>(
            val normalInput: BaseInputs,
            val processingStarted: CompletableDeferred<Unit>,
        ) : Inputs<BaseInputs, State>()

        data class AwaitInput<BaseInputs : Any, State : Any>(
            val normalInput: BaseInputs,
            val processingFinished: CompletableDeferred<State>,
        ) : Inputs<BaseInputs, State>()

        data class TestCompleted<BaseInputs : Any, State : Any>(
            val processingFinished: CompletableDeferred<State>
        ) : Inputs<BaseInputs, State>()
    }
}
