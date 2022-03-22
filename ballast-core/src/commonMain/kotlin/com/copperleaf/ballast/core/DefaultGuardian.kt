package com.copperleaf.ballast.core

import com.copperleaf.ballast.InputStrategy

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
