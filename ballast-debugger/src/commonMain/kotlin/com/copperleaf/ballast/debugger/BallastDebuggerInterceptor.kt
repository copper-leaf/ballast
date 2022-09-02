package com.copperleaf.ballast.debugger

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastInterceptorScope
import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.BallastNotification
import kotlinx.coroutines.flow.Flow

public class BallastDebuggerInterceptor<Inputs : Any, Events : Any, State : Any>(
    private val connection: BallastDebuggerClientConnection<*>,
) : BallastInterceptor<Inputs, Events, State> {

    override fun BallastInterceptorScope<Inputs, Events, State>.start(notifications: Flow<BallastNotification<Inputs, Events, State>>) {
        with(connection) {
            connectViewModel(
                notifications = notifications,
            )
        }
    }

    override suspend fun onNotify(logger: BallastLogger, notification: BallastNotification<Inputs, Events, State>) {}
}
