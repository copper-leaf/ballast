package com.copperleaf.ballast.scheduler.vm

import com.copperleaf.ballast.InputFilter
import com.copperleaf.ballast.InputStrategy
import com.copperleaf.ballast.InputStrategyScope
import com.copperleaf.ballast.Queued
import com.copperleaf.ballast.core.ChannelInputStrategy
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow

/**
 * A sequential first-in-first-out strategy for processing inputs, suitable for background processing. As inputs will be
 * queued instead of running immediately, it is not suitable for processing UI inputs, as the queue could easily be
 * suspended and leave the UI unresponsive. Use a FIFO strategy when you care more that inputs are not dropped, than
 * that they get processed quickly.
 *
 * New inputs will be queued such that the first inputs received will run to completion before later ones start
 * processing. FIFO guarantees that only one input will be processed at a time, and is thus protected against race
 * conditions. Each Input processed with a FIFO strategy can freely access/update the ViewModel state as many times as
 * it needs. FIFO also guarantees that inputs will not be cancelled unless the entire ViewModel gets cancelled.
 *
 * Since we know only 1 Input is being procced at a time, if an input gets cancelled partway through its processing, the
 * ViewModel state will roll back to prevent the ViewModel from being left in a bad state.
 */
public class SchedulerFifoInputStrategy<Inputs : Any, Events : Any, State : Any> private constructor(
    filter: InputFilter<Inputs, Events, State>?
) : ChannelInputStrategy<Inputs, Events, State>(
    capacity = Channel.BUFFERED,
    onBufferOverflow = BufferOverflow.SUSPEND,
    filter = filter,
) {
    override suspend fun InputStrategyScope<Inputs, Events, State>.processInputs(
        filteredQueue: Flow<Queued<Inputs, Events, State>>,
    ) {
        filteredQueue
            .collect { queued ->
                val stateBeforeInput = getCurrentState()

                acceptQueued(queued, Guardian()) {
                    rollbackState(stateBeforeInput)
                }
            }
    }

    public companion object {
        public operator fun invoke(): SchedulerFifoInputStrategy<Any, Any, Any> {
            return SchedulerFifoInputStrategy(null)
        }

        public fun <Inputs : Any, Events : Any, State : Any> typed(filter: InputFilter<Inputs, Events, State>? = null): SchedulerFifoInputStrategy<Inputs, Events, State> {
            return SchedulerFifoInputStrategy(filter)
        }
    }

    public open class Guardian : InputStrategy.Guardian {

        protected var stateAccessed: Boolean = false
        protected var sideJobsPosted: Boolean = false
        protected var usedProperly: Boolean = false
        protected var closed: Boolean = false

        override fun checkStateAccess() {
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
}
