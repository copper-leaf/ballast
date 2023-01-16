package com.copperleaf.ballast.core

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastInterceptorScope
import com.copperleaf.ballast.BallastNotification
import com.copperleaf.ballast.Queued
import com.copperleaf.ballast.awaitViewModelStart
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

public class KillSwitch<Inputs : Any, Events : Any, State : Any>(
    private val gracePeriod: Duration = 100.milliseconds,
) : BallastInterceptor<Inputs, Events, State> {

    private val signal = CompletableDeferred<Unit>()

    override fun BallastInterceptorScope<Inputs, Events, State>.start(
        notifications: Flow<BallastNotification<Inputs, Events, State>>
    ) {
        launch(start = CoroutineStart.UNDISPATCHED) {
            // wait for the BallastNotification.ViewModelStarted to be sent
            notifications.awaitViewModelStart()

            // wait for something to request shutting down the VM
            signal.await()

            sendToQueue(
                Queued.ShutDownGracefully(null, gracePeriod)
            )
        }
    }

    public fun requestGracefulShutdown() {
        signal.complete(Unit)
    }
}
