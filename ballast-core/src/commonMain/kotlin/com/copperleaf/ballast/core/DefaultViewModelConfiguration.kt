package com.copperleaf.ballast.core

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.FilteredInputHandler
import com.copperleaf.ballast.InputFilter
import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputStrategy

public class DefaultViewModelConfiguration<Inputs : Any, Events : Any, State : Any>(
    override val initialState: State,
    override val inputHandler: InputHandler<Inputs, Events, State>,
    override val filter: InputFilter<Inputs, Events, State>? = null,
    override val interceptors: List<BallastInterceptor<Inputs, Events, State>> = emptyList(),
    override val inputStrategy: InputStrategy = LifoInputStrategy(),
    override val name: String = "$inputHandler-vm",
) : BallastViewModelConfiguration<Inputs, Events, State> {
    public constructor(
        initialState: State,
        inputHandler: FilteredInputHandler<Inputs, Events, State>,
        interceptors: List<BallastInterceptor<Inputs, Events, State>> = emptyList(),
        inputStrategy: InputStrategy = LifoInputStrategy(),
        name: String = "$inputHandler-vm",
    ) : this(
        initialState = initialState,
        inputHandler = inputHandler,
        filter = inputHandler,
        interceptors = interceptors,
        inputStrategy = inputStrategy,
        name = name,
    )
}
