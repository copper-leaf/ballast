package com.copperleaf.ballast

public interface BallastViewModelConfiguration<Inputs : Any, Events : Any, State : Any> {
    public val initialState: State
    public val inputHandler: InputHandler<Inputs, Events, State>
    public val filter: InputFilter<Inputs, Events, State>?
    public val interceptor: BallastInterceptor<Inputs, Events, State>?
    public val inputStrategy: InputStrategy
}
