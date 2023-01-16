package com.copperleaf.ballast

import com.copperleaf.ballast.InputFilter.Result
import com.copperleaf.ballast.core.LifoInputStrategy

/**
 * An [InputFilter] is an optional component that can be registered in a ViewModel's [BallastViewModelConfiguration]. If
 * provided, all Inputs are checked against the filter and only those that pass with [Result.Accept] will be delivered
 * to [InputStrategy], and eventually to the [InputHandler].
 *
 * This has the effect of allowing you to selectively reject inputs and prevent them from ever being delivered to the
 * [InputHandler] if the State is not set up such that it can process it. Consider the following scenario:
 *
 * You are using a [LifoInputStrategy] so only 1 Input will be processing at a time, and new Inputs will cancel any
 * running ones. You process an Input that sets `state.loading` to true, makes and API call, and then sets it back to
 * false. During that time, you wish to ignore any further requests to ensure that the API call runs to completion and
 * does not get interrupted. You'd think you could just check for `state.loading == true` when the Input comes in to the
 * [InputHandler], but by the time that happens it's already too late and the Input making the API call has already been
 * cancelled. But you can make that same check inside an [InputFilter], returning [Result.Reject] for all Inputs when
 * `state.loading == true` which will prevent those Inputs from being passed to the [InputStrategy] and cancelling the
 * current one.
 */
public interface InputFilter<Inputs : Any, Events : Any, State : Any> {

    /**
     * Test if this Input can be accepted while in the current state. Events that are accepted will cancel any
     * currently-running tasks. A simple boolean flag for `busy` is usually sufficient to prevent unintended
     * cancellation of long-running tasks, but more complex screens may benefit from constructing an explicit Finite
     * State Machine and testing the input against that to see if it can be accepted.
     *
     * @return true to accept this Input, cancel the current one, and proceed to handle it. False to
     *  drop the Input without cancelling the current one.
     */
    public fun filterInput(state: State, input: Inputs): Result

    public enum class Result {
        Accept,
        Reject,
    }
}
