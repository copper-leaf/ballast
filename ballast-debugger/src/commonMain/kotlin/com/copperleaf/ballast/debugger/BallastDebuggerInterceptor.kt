package com.copperleaf.ballast.debugger

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastNotification
import com.copperleaf.ballast.Queued
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

@ExperimentalTime
public class BallastDebuggerInterceptor<Inputs : Any, Events : Any, State : Any>(
    private val applicationCoroutineScope: CoroutineScope,
    private val connection: BallastDebuggerClientConnection<*>,
) : BallastInterceptor<Inputs, Events, State> {

    override fun start(
        viewModelScope: CoroutineScope,
        notifications: Flow<BallastNotification<Inputs, Events, State>>,
        sendToQueue: suspend (Queued<Inputs, Events, State>) -> Unit
    ) {
        applicationCoroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
            notifications
                .collect(::onNotify)
        }
    }

    override suspend fun onNotify(notification: BallastNotification<Inputs, Events, State>) {
        connection.acceptNotification(notification)
    }
}
