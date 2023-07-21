package com.copperleaf.ballast

public interface EventStrategyScope<Inputs : Any, Events : Any, State : Any> {
    public val logger: BallastLogger

    public suspend fun dispatchEvent(event: Events)
}
