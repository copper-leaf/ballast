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
    public fun <T> createChannel(): Channel<T>

    /**
     * When an input gets cancelled, should the state be rolled back to where it was before the input was accepted? If
     * the strategy guarantees that inputs will always be run sequentially, never in parallel, then this should be true.
     * Otherwise, if inputs are executing in parallel, we can't meaningfully know what state we should roll back to,
     * since another input have updated the state since we took a snapshot of the state before processing an input.
     */
    public val rollbackOnCancellation: Boolean

    /**
     * Collect the inputs that have been sent to the ViewModel and process each of them, typically using traditional
     * Flow operators internally. [filteredInputs] is the Flow of inputs that are being received from the ViewModel's
     * Channel, and have already been filtered according to the ViewModel's [InputFilter] if a filter was provided.
     *
     * Once an input has been received, it should be sent back to the ViewModel through [acceptInput] for internal
     * processing. Once the internal processing has completed, if it ran to completion successfully, [onCompleted] will
     * be called with the result of processing, which can be used to determine if the input was handled properly
     * according to the restrictions of the InputStrategy.
     */
    public suspend fun <Inputs : Any> processInputs(
        filteredInputs: Flow<Inputs>,
        acceptInput: suspend (input: Inputs, onCompleted: (InputStrategy.InputResult) -> Unit) -> Unit,
    )

    /**
     * The results of processing an input to completion.
     */
    public data class InputResult(
        /**
         * A count of how many times this input attempted to read and/or update the ViewModel State.
         */
        val stateUpdatesPerformed: Int
    )
}
