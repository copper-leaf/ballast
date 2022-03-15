package com.copperleaf.ballast

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

public interface BallastInterceptor<Inputs : Any, Events : Any, State : Any> {

    public fun start(
        hostViewModelName: String,
        viewModelScope: CoroutineScope,
        notifications: Flow<BallastNotification<Inputs, Events, State>>,
        sendToQueue: suspend (Queued<Inputs, Events, State>) -> Unit,
    ) {
        viewModelScope.launch(start = CoroutineStart.UNDISPATCHED) {
            notifications.collect(::onNotify)
        }
    }

    public suspend fun onNotify(notification: BallastNotification<Inputs, Events, State>) { }
}
