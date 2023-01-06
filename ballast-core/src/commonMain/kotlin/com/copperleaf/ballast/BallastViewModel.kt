package com.copperleaf.ballast

import com.copperleaf.ballast.internal.BallastViewModelImpl
import kotlinx.coroutines.channels.ChannelResult
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.StateFlow

/**
 * [BallastViewModel] is the main class that implements the Ballast MVI model. See the
 * [official documentation](https://copper-leaf.github.io/ballast/) for full usage instructions, examples, and then
 * mental model to use when programming your application with Ballast.
 *
 * This interface should not be implemented directly by your application code. Generally-speaking, you will use
 * platform-specific abstract classes which implement this interface according to that platform's needs, and you will
 * extend that class instead. This should only be implemented directly if you are creating a custom base class that your
 * application's ViewModels must extend.
 *
 * Practically-speaking, those platform-specific ViewModels just wrap an instance of [BallastViewModelImpl], which does
 * the actual work of implementing the pattern, and delegates all its internal calls to that internal implementation.
 */
public interface BallastViewModel<Inputs : Any, Events : Any, State : Any> {

    /**
     * Observe the flow of states from this ViewModel
     */
    public fun observeStates(): StateFlow<State>

    /**
     * Posts an Input to this ViewModel's Input Queue immediately without suspending using [SendChannel.trySend]. If
     * the input channel's buffer is full or if the ViewModel is cleared, the input will be dropped, as reported by the
     * returned [ChannelResult].
     */
    public fun trySend(element: Inputs): ChannelResult<Unit>

    /**
     * Posts an Input to this ViewModel's Input Queue using [SendChannel.send], suspending the caller while the buffer
     * of this channel is full.
     */
    public suspend fun send(element: Inputs)

    /**
     * Posts an Input to this ViewModel's Input Queue using [SendChannel.send]. This method will suspend until the Input
     * has finished processing completely.
     */
    public suspend fun sendAndAwaitCompletion(element: Inputs)
}
