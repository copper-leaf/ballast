package com.copperleaf.ballast.contracts.dispatcher

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastInterceptorScope
import com.copperleaf.ballast.BallastNotification
import com.copperleaf.ballast.Queued
import com.copperleaf.ballast.awaitViewModelStart
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class DispatcherTestInterceptor : BallastInterceptor<
        DispatcherTestContract.Inputs,
        DispatcherTestContract.Events,
        DispatcherTestContract.State> {

    override fun BallastInterceptorScope<
            DispatcherTestContract.Inputs,
            DispatcherTestContract.Events,
            DispatcherTestContract.State>.start(
        notifications: Flow<BallastNotification<
                DispatcherTestContract.Inputs,
                DispatcherTestContract.Events,
                DispatcherTestContract.State>>,
    ) {
        launch(start = CoroutineStart.UNDISPATCHED) {
            notifications.awaitViewModelStart()

            sendToQueue(
                Queued.HandleInput(
                    null,
                    DispatcherTestContract.Inputs.SetInterceptorDispatcher(
                        actualInterceptorCoroutineScopeInfo = getCoroutineScopeInfo()
                    )
                )
            )
        }
    }
}
