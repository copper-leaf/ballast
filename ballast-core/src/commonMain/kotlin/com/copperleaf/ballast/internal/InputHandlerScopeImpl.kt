package com.copperleaf.ballast.internal

import com.copperleaf.ballast.InputHandlerScope
import com.copperleaf.ballast.InputStrategy
import com.copperleaf.ballast.SideEffectScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet

internal class InputHandlerScopeImpl<Inputs : Any, Events : Any, State : Any>(
    private val guardian: InputStrategy.Guardian,
    private val _state: MutableStateFlow<State>,
    private val sendEventToQueue: suspend (Events) -> Unit,
) : InputHandlerScope<Inputs, Events, State> {
    private var closed = false
    private var usedProperly = false

    private val sideEffects = mutableListOf<SideEffectRequest<Inputs, Events, State>>()

    override suspend fun getCurrentState(): State {
        checkNotClosed()
        checkNoSideEffects()
        guardian.checkStateAccess()
        return _state.value
    }

    override suspend fun updateState(block: (State) -> State) {
        checkNotClosed()
        checkNoSideEffects()
        guardian.checkStateAccess()
        _state.update(block).also { usedProperly = true }
    }

    override suspend fun updateStateAndGet(block: (State) -> State): State {
        checkNotClosed()
        checkNoSideEffects()
        guardian.checkStateAccess()
        return _state.updateAndGet(block).also { usedProperly = true }
    }

    override suspend fun getAndUpdateState(block: (State) -> State): State {
        checkNotClosed()
        checkNoSideEffects()
        guardian.checkStateAccess()
        return _state.getAndUpdate(block).also { usedProperly = true }
    }

    override suspend fun postEvent(event: Events) {
        checkNotClosed()
        checkNoSideEffects()
        sendEventToQueue(event).also { usedProperly = true }
    }

    override fun sideEffect(
        key: String,
        block: suspend SideEffectScope<Inputs, Events, State>.() -> Unit
    ) {
        checkNotClosed()
        sideEffects += SideEffectRequest(key, block)
        usedProperly = true
    }

    override fun noOp() {
        checkNotClosed()
        checkNoSideEffects()
        usedProperly = true
    }

    private fun checkNoSideEffects() {
        check(sideEffects.isEmpty()) {
            "Side-Effects must be the last statements of the InputHandler"
        }
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

    internal fun close(): List<SideEffectRequest<Inputs, Events, State>> {
        checkNotClosed()
        checkUsedProperly()
        closed = true
        return sideEffects.toList()
    }
}
