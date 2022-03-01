package com.copperleaf.ballast

import com.copperleaf.ballast.core.FifoInputStrategy
import com.copperleaf.ballast.core.LifoInputStrategy
import com.copperleaf.ballast.core.ParallelInputStrategy
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow

/**
 * Ballast ViewModels are designed to be safe and prevent you from doing things that could cause hard-to-debug race
 * conditions and break the purity of the MVI "state machine". But there are several ways to do this safely, though each
 * has their own set of pros/cons. By providing a different InputStrategy to your Ballast ViewModels, you can choose
 * which set of tradeoffs you are willing to accept, or you can define your own strategy customized to your needs.
 *
 * See the following links for the available core input strategies. By default, [LifoInputStrategy], which is suitable
 * for UI-bound ViewModels.
 *
 * @see [LifoInputStrategy]
 * @see [FifoInputStrategy]
 * @see [ParallelInputStrategy]
 */
public interface InputStrategy {

    /**
     * Create the ViewModel channel most appropriate for accepting Inputs to the ViewModel and passing them to the
     * internal processing pipeline.
     */
    public fun <T> createQueue(): Channel<T>

    /**
     * When an input gets cancelled, should the state be rolled back to where it was before the input was accepted? If
     * the strategy guarantees that inputs will always be run sequentially, never in parallel, then this should be true.
     * Otherwise, if inputs are executing in parallel, we can't meaningfully know what state we should roll back to,
     * since another input have updated the state since we took a snapshot of the state before processing an input.
     */
    public val rollbackOnCancellation: Boolean

    /**
     * Collect the inputs that have been sent to the ViewModel and process each of them, typically using traditional
     * Flow operators internally. [filteredQueue] is the Flow of inputs that are being received from the ViewModel's
     * Channel, and have already been filtered according to the ViewModel's [InputFilter] if a filter was provided.
     *
     * Once an input has been received, it should be sent back to the ViewModel through [acceptQueued] for internal
     * processing. The Strategy will provide a Guardian to the [InputHandlerScope], to ensure the Input is being handled
     * safely according to its own rules, guarding against potential issues.
     */
    public suspend fun <Inputs : Any, Events : Any, State : Any> processInputs(
        filteredQueue: Flow<Queued<Inputs, Events, State>>,
        acceptQueued: suspend (queued: Queued<Inputs, Events, State>, guardian: Guardian) -> Unit,
    )

    /**
     * A Guardian protects the integrity of the ViewModel state against potential problems, especially with race
     * conditions due to parallel processing.
     */
    public interface Guardian {

        /**
         * Checked on every call to [InputHandlerScope.getCurrentState].
         */
        public fun checkStateAccess() { }

        /**
         * Checked on every call to [InputHandlerScope.updateState], [InputHandlerScope.getAndUpdateState], or
         * [InputHandlerScope.updateStateAndGet].
         */
        public fun checkStateUpdate() { }
    }
}
