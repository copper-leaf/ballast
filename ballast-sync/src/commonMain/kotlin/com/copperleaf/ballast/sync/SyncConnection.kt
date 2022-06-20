package com.copperleaf.ballast.sync

import com.copperleaf.ballast.BallastInterceptorScope
import com.copperleaf.ballast.BallastNotification
import kotlinx.coroutines.flow.Flow

/**
 * A generic connection to the syncrhonization service. It is entirely up to the implementation to define how to handle
 * synchronization.
 *
 * For out-of-the-box usage, consider using [DefaultSyncConnection] with [InMemorySyncAdapter] to synchronize ViewModels
 * within a single application.
 */
public interface SyncConnection<
    Inputs : Any,
    Events : Any,
    State : Any> {

    public fun BallastInterceptorScope<Inputs, Events, State>.connectViewModel(
        clientType: SyncClientType,
        notifications: Flow<BallastNotification<Inputs, Events, State>>,
    )
}
