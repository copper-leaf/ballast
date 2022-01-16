package com.copperleaf.ballast.core

import com.copperleaf.ballast.InputStrategy
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Unlike LIFO and FIFO strategies, Parallel gives no guarantee that inputs will be processed in any given order or
 * that only 1 Input will be processing at a time. However, it allows you to have inputs that both: start running
 * immediately without getting blocked, while also guaranteeing that long-running inputs will not get cancelled.
 * Parallel is suitable for processing complex UIs that have many Input sources and many long-running tasks, but should
 * not be the first choice to UI ViewModels. Prefer a LIFO strategy with an input filter for UIs, and only use this in
 * specific scenarios where that becomes difficult to manage. Also consider moving long-running work to a Side Effect
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
public class ParallelInputStrategy : InputStrategy {

    override fun <T> createChannel(): Channel<T> {
        return Channel(Channel.BUFFERED, BufferOverflow.SUSPEND)
    }

    override val rollbackOnCancellation: Boolean = false

    override suspend fun <Inputs : Any> processInputs(
        filteredInputs: Flow<Inputs>,
        acceptInput: suspend (input: Inputs, onCompleted: (InputStrategy.InputResult) -> Unit) -> Unit,
    ) {
        coroutineScope {
            val viewModelScope = this

            filteredInputs
                .collect { input ->
                    viewModelScope.launch {
                        acceptInput(input) { result ->
                            check(result.stateUpdatesPerformed in 0..1) {
                                "ParallelInputStrategy requires that inputs only access or update the state at most " +
                                    "once as a safeguard against race conditions."
                            }
                        }
                    }
                }
        }
    }
}
