package com.copperleaf.ballast.core

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastInterceptorScope
import com.copperleaf.ballast.BallastNotification
import com.copperleaf.ballast.Queued
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

public class BootstrapInterceptor<Inputs : Any, Events : Any, State : Any>(
    private val getInitialInput: suspend () -> Inputs,
) : BallastInterceptor<Inputs, Events, State> {

    override fun BallastInterceptorScope<Inputs, Events, State>.start(
        notifications: Flow<BallastNotification<Inputs, Events, State>>
    ) {
        launch(start = CoroutineStart.UNDISPATCHED) {
            // wait for the BallastNotification.ViewModelStarted to be sent
            notifications
                .filterIsInstance<BallastNotification.ViewModelStarted<Inputs, Events, State>>()
                .first()

            // generate an Input
            val initialInput = getInitialInput()

            // post the Input back to the VM
            sendToQueue(
                Queued.HandleInput(null, initialInput)
            )
        }
    }
}
