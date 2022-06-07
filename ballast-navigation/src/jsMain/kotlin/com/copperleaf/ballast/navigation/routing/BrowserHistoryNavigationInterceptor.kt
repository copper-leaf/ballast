package com.copperleaf.ballast.navigation.routing

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastInterceptorScope
import com.copperleaf.ballast.BallastNotification
import com.copperleaf.ballast.navigation.routing.RouterContract
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.w3c.dom.PopStateEvent

public class BrowserHistoryNavigationInterceptor : BallastInterceptor<
    RouterContract.Inputs,
    RouterContract.Events,
    RouterContract.State,
    > {

    override fun BallastInterceptorScope<
        RouterContract.Inputs,
        RouterContract.Events,
        RouterContract.State,
        >.start(
        notifications: Flow<BallastNotification<
            RouterContract.Inputs,
            RouterContract.Events,
            RouterContract.State,
            >>
    ) {
        launch(start = CoroutineStart.UNDISPATCHED) {
            notifications
                .filterIsInstance<BallastNotification.StateChanged<
                    RouterContract.Inputs,
                    RouterContract.Events,
                    RouterContract.State,
                    >>()
                .map { it.state.currentDestination }
                .distinctUntilChanged()
                .onEach {
                    window.history.pushState(null, "", it?.path)
                }
                .launchIn(this)
        }
    }

    private fun onPopStateAsFlow(): Flow<PopStateEvent> {
        return callbackFlow {
            val callback = { event: PopStateEvent ->
                this@callbackFlow.trySend(event)

                Unit
            }
            window.onpopstate = callback

            awaitClose {
                window.onpopstate = null
            }
        }
    }
}
