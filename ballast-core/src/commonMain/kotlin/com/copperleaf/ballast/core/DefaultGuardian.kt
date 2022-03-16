package com.copperleaf.ballast.core

import com.copperleaf.ballast.InputStrategy

public open class DefaultGuardian : InputStrategy.Guardian {

    protected var stateAccessed: Boolean = false
    protected var sideEffectsPosted: Boolean = false
    protected var usedProperly: Boolean = false
    protected var closed: Boolean = false

    override fun checkStateAccess() {
        checkNotClosed()
        checkNoSideEffects()
        stateAccessed = true
        usedProperly = true
    }

    override fun checkStateUpdate() {
        checkNotClosed()
        checkNoSideEffects()
        stateAccessed = true
        usedProperly = true
    }

    override fun checkPostEvent() {
        checkNotClosed()
        checkNoSideEffects()
        usedProperly = true
    }

    override fun checkNoOp() {
        checkNotClosed()
        checkNoSideEffects()
        usedProperly = true
    }

    override fun checkSideEffect() {
        checkNotClosed()
        sideEffectsPosted = true
        usedProperly = true
    }

    override fun close() {
        checkNotClosed()
        checkUsedProperly()
        closed = true
    }

// Inner checks
// ---------------------------------------------------------------------------------------------------------------------

    private fun checkNotClosed() {
        check(!closed) { "This InputHandlerScope has already been closed" }
    }

    private fun checkNoSideEffects() {
        check(!sideEffectsPosted) {
            "Side-Effects must be the last statements of the InputHandler"
        }
    }

    private fun checkUsedProperly() {
        check(usedProperly) {
            "Input was not handled properly. To ensure you're following the MVI model properly, make sure any " +
                "side-effects are executed in a `sideEffect { }` block."
        }
    }
}
