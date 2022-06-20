package com.copperleaf.ballast.sync

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastInterceptorScope
import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.BallastNotification
import kotlinx.coroutines.flow.Flow

/**
 * The Interceptor to configure a ViewModel to participate in real-time synchronization. The [connection] defines the
 * method by which the ViewModels are synchronized with each other, and [clientType] defines the role that this
 * ViewModel instance plays in that process.
 */
public class BallastSyncInterceptor<
    Inputs : Any,
    Events : Any,
    State : Any>(
    private val connection: SyncConnection<Inputs, Events, State>,
    private val clientType: SyncClientType,
) : BallastInterceptor<Inputs, Events, State> {

    override fun BallastInterceptorScope<Inputs, Events, State>.start(notifications: Flow<BallastNotification<Inputs, Events, State>>) {
        with(connection) {
            connectViewModel(
                clientType = clientType,
                notifications = notifications,
            )
        }
    }

    override suspend fun onNotify(logger: BallastLogger, notification: BallastNotification<Inputs, Events, State>) {}
}
