package com.copperleaf.ballast.internal

import com.copperleaf.ballast.InputHandlerScope
import com.copperleaf.ballast.InputStrategy
import com.copperleaf.ballast.SideEffectScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet

internal class InputHandlerScopeImpl<Inputs : Any, Events : Any, State : Any>(
    private val _state: MutableStateFlow<State>,
    private val _events: SendChannel<Events>,
) : InputHandlerScope<Inputs, Events, State> {
    private var closed = false
    private var usedProperly = false

    private val sideEffects = mutableListOf<RestartableSideEffect<Inputs, Events, State>>()

    private var stateAccesses = 0

    override suspend fun getCurrentState(): State {
        stateAccesses++
        return _state.value
    }

    override suspend fun updateState(block: (State) -> State) {
        checkNotClosed()
        stateAccesses++
        _state.update(block).also { usedProperly = true }
    }

    override suspend fun updateStateAndGet(block: (State) -> State): State {
        checkNotClosed()
        stateAccesses++
        return _state.updateAndGet(block).also { usedProperly = true }
    }

    override suspend fun getAndUpdateState(block: (State) -> State): State {
        checkNotClosed()
        stateAccesses++
        return _state.getAndUpdate(block).also { usedProperly = true }
    }

    override suspend fun postEvent(event: Events) {
        checkNotClosed()
        _events.send(event).also { usedProperly = true }
    }

    override fun sideEffect(
        key: String?,
        onRestarted: suspend () -> Unit,
        block: suspend SideEffectScope<Inputs, Events, State>.(State) -> Unit
    ) {
        checkNotClosed()
        sideEffects += RestartableSideEffect(key, onRestarted, block)
        usedProperly = true
    }

    override fun noOp() {
        checkNotClosed()
        usedProperly = true
    }

    private fun checkNotClosed() {
        check(!closed) { "This InputHandlerScope has already been closed" }
    }

    private fun checkUsedProperly() {
        check(usedProperly) {
            "Input was not handled properly. To ensure you're following the MVI model properly, make sure any " +
                "side-effects are executed in a `sideEffect { }` block."
        }
    }

    internal fun close(): List<RestartableSideEffect<Inputs, Events, State>> {
        checkNotClosed()
        checkUsedProperly()
        closed = true
        return sideEffects.toList()
    }

    internal fun getResult(): InputStrategy.InputResult {
        return InputStrategy.InputResult(stateAccesses)
    }
}
