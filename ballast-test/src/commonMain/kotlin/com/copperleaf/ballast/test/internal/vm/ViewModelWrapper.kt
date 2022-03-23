package com.copperleaf.ballast.test.internal.vm

import com.copperleaf.ballast.BallastViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.ChannelResult
import kotlinx.coroutines.flow.StateFlow

internal class ViewModelWrapper<Inputs : Any, Events : Any, State : Any>(
    private val delegate: BallastViewModel<TestViewModel.Inputs<Inputs>, Events, State>,
) : BallastViewModel<Inputs, Events, State> {

    override val name: String
        get() = delegate.name
    override val type: String
        get() = delegate.type

    override suspend fun send(element: Inputs) {
        delegate.send(TestViewModel.Inputs.ProcessInput(element, CompletableDeferred()))
    }

    override suspend fun sendAndAwaitCompletion(element: Inputs) {
        delegate.sendAndAwaitCompletion(TestViewModel.Inputs.ProcessInput(element, CompletableDeferred()))
    }

    override fun trySend(element: Inputs): ChannelResult<Unit> {
        return delegate.trySend(TestViewModel.Inputs.ProcessInput(element, CompletableDeferred()))
    }

    override fun observeStates(): StateFlow<State> {
        return delegate.observeStates()
    }

}
