package com.copperleaf.ballast.test.internal.vm

import com.copperleaf.ballast.BallastViewModel
import com.copperleaf.ballast.internal.BallastViewModelImpl
import kotlinx.coroutines.CompletableDeferred

internal class TestViewModel<Inputs : Any, Events : Any, State : Any> internal constructor(
    internal val impl: BallastViewModelImpl<TestViewModel.Inputs<Inputs>, Events, State>
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
