package com.copperleaf.ballast

/**
 * An InputHandler that also filters its inputs to protect against unwanted cancellation.
 */
public interface FilteredInputHandler<Inputs : Any, Events : Any, State : Any> :
    InputHandler<Inputs, Events, State>,
    InputFilter<Inputs, Events, State>
