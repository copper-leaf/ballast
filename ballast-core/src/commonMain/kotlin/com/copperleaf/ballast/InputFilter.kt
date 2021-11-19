package com.copperleaf.ballast

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
        Drop,
    }
}
