package com.copperleaf.ballast.core

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.FilteredInputHandler
import com.copperleaf.ballast.InputFilter
import com.copperleaf.ballast.InputHandler

public class DefaultViewModelConfiguration<Inputs : Any, Events : Any, State : Any>(
    override val inputHandler: InputHandler<Inputs, Events, State>,
    override val filter: InputFilter<Inputs, Events, State>? = null,
    override val interceptor: BallastInterceptor<Inputs, Events, State>? = null,
) : BallastViewModelConfiguration<Inputs, Events, State> {
    public constructor(
        inputHandler: FilteredInputHandler<Inputs, Events, State>,
        interceptor: BallastInterceptor<Inputs, Events, State>? = null,
    ) : this(inputHandler, inputHandler, interceptor)
}
