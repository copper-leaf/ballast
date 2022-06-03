package com.copperleaf.ballast.core

import com.copperleaf.ballast.InputStrategy

/**
 * A default [InputStrategy.Guardian] that may be used by a custom [InputStrategy] for ensuring Inputs are handled the
 * same way as the built-in strategies. It can be overridden to make slight tweaks to its behavior, but larger tweaks
 * should be done by creating a fully-custom Guardian implementation.
 *
 * This guardian enforces the following rules:
 *
 * - Nothing gets changed after the InputHandler returns from processing an Input (such as coroutine launched in
 *      parallel attempting to use a reference to the original InputHandlerScope).
 * - Starting a SideJob must be the last statement of the InputHandler. You can start multiple side-jobs from a single
 *      Input, but all of them must be the last statements of the InputHandler. This helps prevent issues from the fact
 *      that SideJobs are running as a lambda, but do not get started immediately when `sideJob()` is called.
 * - There should be _something_ done during the processing of an event. This helps prevent Input types from
 *      accidentally getting ignored, or from using an Input incorrectly (updating variables anywhere other than the VM
 *      State, launching coroutines rather than using SideJobs, etc.)
 */
public open class DefaultGuardian : InputStrategy.Guardian {

    protected var stateAccessed: Boolean = false
    protected var sideJobsPosted: Boolean = false
    protected var usedProperly: Boolean = false
    protected var closed: Boolean = false

    override fun checkStateAccess() {
        checkNotClosed()
        checkNoSideJobs()
        stateAccessed = true
        usedProperly = true
    }

    override fun checkStateUpdate() {
        checkNotClosed()
        checkNoSideJobs()
        stateAccessed = true
        usedProperly = true
    }

    override fun checkPostEvent() {
        checkNotClosed()
        checkNoSideJobs()
        usedProperly = true
    }

    override fun checkNoOp() {
        checkNotClosed()
        checkNoSideJobs()
        usedProperly = true
    }

    override fun checkSideJob() {
        checkNotClosed()
        sideJobsPosted = true
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

    private fun checkNoSideJobs() {
        check(!sideJobsPosted) {
            "Side-Jobs must be the last statements of the InputHandler"
        }
    }

    private fun checkUsedProperly() {
        check(usedProperly) {
            "Input was not handled properly. To ensure you're following the MVI model properly, make sure any " +
                "side-jobs are executed in a `sideJob { }` block."
        }
    }
}
