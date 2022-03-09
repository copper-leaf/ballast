package com.copperleaf.ballast.test.internal.vm

import com.copperleaf.ballast.BallastViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ChannelResult
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.selects.SelectClause2

internal class ViewModelWrapper<Inputs : Any, Events : Any, State : Any>(
    private val delegate: BallastViewModel<TestViewModel.Inputs<Inputs>, Events, State>,
) : BallastViewModel<Inputs, Events, State> {

    override val name: String
        get() = delegate.name

    @ExperimentalCoroutinesApi
    override val isClosedForSend: Boolean
        get() = delegate.isClosedForSend

    override val onSend: SelectClause2<Inputs, SendChannel<Inputs>>
        get() = error("cannot call onSend on ViewModelWrapper")

    override fun close(cause: Throwable?): Boolean {
        return delegate.close(cause)
    }

    @ExperimentalCoroutinesApi
    override fun invokeOnClose(handler: (cause: Throwable?) -> Unit) {
        delegate.invokeOnClose(handler)
    }

    override suspend fun send(element: Inputs) {
        delegate.send(TestViewModel.Inputs.ProcessInput(element, CompletableDeferred()))
    }

    override fun trySend(element: Inputs): ChannelResult<Unit> {
        return delegate.trySend(TestViewModel.Inputs.ProcessInput(element, CompletableDeferred()))
    }

    override fun observeStates(): StateFlow<State> {
        return delegate.observeStates()
    }
}
