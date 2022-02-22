package com.copperleaf.ballast

public interface BallastInterceptor<Inputs : Any, Events : Any, State : Any> {

    public suspend fun onNotify(notification: BallastNotification<Inputs, Events, State>)
}
