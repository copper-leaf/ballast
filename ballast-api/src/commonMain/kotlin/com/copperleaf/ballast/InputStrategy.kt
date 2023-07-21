package com.copperleaf.ballast

import com.copperleaf.ballast.core.FifoInputStrategy
import com.copperleaf.ballast.core.LifoInputStrategy
import com.copperleaf.ballast.core.ParallelInputStrategy
import kotlinx.coroutines.channels.ChannelResult

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
public interface InputStrategy<Inputs : Any, Events : Any, State : Any> {

    /**
     * Start the InputStrategy and allow the ViewModel to begin accepting Inputs.
     */
    public fun InputStrategyScope<Inputs, Events, State>.start()

    /**
     * Schedule [queued] for processing. This method will suspend until the value has been successfully placed into the
     * queue, not necessarily until it starts or ends processing. Use the [Queued.deferred] to wait until it has been
     * fully handled. This should be used to provide backpressure to the queue for strategies that need it.
     */
    public suspend fun enqueue(
        queued: Queued<Inputs, Events, State>,
    )

    /**
     * Schedule [queued] for processing without waiting for it to be placed in the queue. A [ChannelResult] will be
     * returned to notify you of whether the value was placed into the queue, or whether the buffer was full and was
     * dropped. This is the non-suspending version of [enqueue] that is unable to provide backpressure.
     */
    public fun tryEnqueue(
        queued: Queued<Inputs, Events, State>,
    ): ChannelResult<Unit>

    /**
     * Immediately mark the InputStrategy as closed. Aftr this function returns, no more Inputs may be sent to the VM,
     * though anything currently in the queue may still be processed.
     */
    public fun close()

    /**
     * Suspend until everything in the queue has been fully processed.
     */
    public suspend fun flush()

    /**
     * A Guardian protects the integrity of the ViewModel state against potential problems, especially with race
     * conditions due to parallel processing.
     */
    public interface Guardian {

        /**
         * Checked on every call to [InputHandlerScope.getCurrentState].
         */
        public fun checkStateAccess() {}

        /**
         * Checked on every call to [InputHandlerScope.updateState], [InputHandlerScope.getAndUpdateState], or
         * [InputHandlerScope.updateStateAndGet].
         */
        public fun checkStateUpdate() {}

        /**
         * Checked on every call to [InputHandlerScope.sideJob].
         */
        public fun checkSideJob() {}

        /**
         * Checked on every call to [InputHandlerScope.postEvent].
         */
        public fun checkPostEvent() {}

        /**
         * Checked on every call to [InputHandlerScope.noOp].
         */
        public fun checkNoOp() {}

        /**
         * Called once the Input has finished processing and the InputHandler has returned. That [InputHandlerScope] is
         * now closed and attempts to interact with it any further should fail, ensuring that it is not accidentally
         * referenced and accessed by a background coroutine beyond its intended lifetime.
         */
        public fun close() {}
    }
}
