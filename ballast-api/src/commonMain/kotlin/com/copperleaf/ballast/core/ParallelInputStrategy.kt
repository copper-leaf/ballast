package com.copperleaf.ballast.core

import com.copperleaf.ballast.InputStrategy
import com.copperleaf.ballast.InputStrategyScope
import com.copperleaf.ballast.Queued
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Unlike LIFO and FIFO strategies, Parallel gives no guarantee that inputs will be processed in any given order or
 * that only 1 Input will be processing at a time. However, it allows you to have inputs that both: start running
 * immediately without getting blocked, while also guaranteeing that long-running inputs will not get cancelled.
 * Parallel is suitable for processing complex UIs that have many Input sources and many long-running tasks, but should
 * not be the first choice to UI ViewModels. Prefer a LIFO strategy with an input filter for UIs, and only use this in
 * specific scenarios where that becomes difficult to manage. Also consider moving long-running work to a side-job
 * instead of dropping the entire ViewModel into parallel-processing mode, if that work doesn't need to perform state
 * updates.
 *
 * However, since inputs are being processed in parallel, it allows the possibility of race conditions if multiple
 * inputs update the ViewModel state, but get interleaved with each other. To prevent this and protect the purity of the
 * "state machine" where everything is deterministic with respect to the ordering of inputs, this strategy adds a
 * restriction that each Input may only access or update the ViewModel state at most 1 time. Individual State accesses
 * and updates are atomic and so are protected against race conditions, but performing multiple updates would require
 * some kind of synchronization to be safe, which goes against the state machine philosophy.
 *
 * Because multiple inputs may be processed at once, if an input is cancelled there is no meaningful way to know what
 * state should be rolled-back to. Cancelled inputs may leave the ViewModel in a bad state.
 */
public class ParallelInputStrategy<Inputs : Any, Events : Any, State : Any> private constructor(): InputStrategy<Inputs, Events, State> {

    override fun createQueue(): Channel<Queued<Inputs, Events, State>> {
        return Channel(Channel.BUFFERED, BufferOverflow.SUSPEND)
    }

    override val rollbackOnCancellation: Boolean = false

    override suspend fun InputStrategyScope<Inputs, Events, State>.processInputs(
        filteredQueue: Flow<Queued<Inputs, Events, State>>,
    ) {
        coroutineScope {
            val viewModelScope = this

            filteredQueue
                .collect { queued ->
                    viewModelScope.launch {
                        acceptQueued(queued, Guardian())
                    }
                }
        }
    }

    public class Guardian : DefaultGuardian() {
        private fun performStateAccessCheck() {
            check(!stateAccessed) {
                "ParallelInputStrategy requires that inputs only access or update the state at most once as a " +
                    "safeguard against race conditions."
            }
        }

        override fun checkStateAccess() {
            performStateAccessCheck()
            super.checkStateAccess()
        }

        override fun checkStateUpdate() {
            performStateAccessCheck()
            super.checkStateUpdate()
        }
    }

    public companion object {
        public operator fun invoke() : ParallelInputStrategy<Any, Any, Any> {
            return ParallelInputStrategy()
        }

        public fun <Inputs : Any, Events : Any, State : Any> typed(): ParallelInputStrategy<Inputs, Events, State> {
            return ParallelInputStrategy()
        }
    }
}
