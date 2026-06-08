package com.copperleaf.ballast.queue

import com.copperleaf.ballast.InputStrategy

internal class JobQueueGuardian<Events, State>(
    internal val queueExecutorScope: QueueExecutorScope<State>
) : InputStrategy.Guardian {

    private var stateAccessed: Boolean = false
    private var sideJobsPosted: Boolean = false
    private var usedProperly: Boolean = false
    private var closed: Boolean = false
    internal var resultEvent: Events? = null

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

    internal fun setEventAsResult(event: Events) {
        if (resultEvent == null) {
            resultEvent = event
        } else {
            error(
                "The Queue's InputHandler attempted to post multiple Events as results of a single Input. Only one " +
                        "Event can be posted as a result of handling an Input."
            )
        }
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
