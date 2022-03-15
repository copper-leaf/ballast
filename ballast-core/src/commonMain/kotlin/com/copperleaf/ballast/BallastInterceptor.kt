package com.copperleaf.ballast

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

public interface BallastInterceptor<Inputs : Any, Events : Any, State : Any> {

    public fun BallastInterceptorScope<Inputs, Events, State>.start(
        notifications: Flow<BallastNotification<Inputs, Events, State>>,
    ) {
        launch(start = CoroutineStart.UNDISPATCHED) {
            notifications.collect {
                onNotify(logger, it)
            }
        }
    }

    public suspend fun onNotify(logger: BallastLogger, notification: BallastNotification<Inputs, Events, State>) {}
}
