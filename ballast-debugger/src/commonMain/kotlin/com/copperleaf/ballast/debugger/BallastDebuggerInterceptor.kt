package com.copperleaf.ballast.debugger

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastNotification
import kotlin.time.ExperimentalTime

@ExperimentalTime
public class BallastDebuggerInterceptor<Inputs : Any, Events : Any, State : Any>(
    private val connection: BallastDebuggerClientConnection<*>,
) : BallastInterceptor<Inputs, Events, State> {

    override suspend fun onNotify(notification: BallastNotification<Inputs, Events, State>) {
        connection.acceptNotification(notification)
    }
}
