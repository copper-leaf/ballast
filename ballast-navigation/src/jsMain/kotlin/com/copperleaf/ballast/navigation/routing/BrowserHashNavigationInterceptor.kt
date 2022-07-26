package com.copperleaf.ballast.navigation.routing

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastInterceptorScope
import com.copperleaf.ballast.BallastNotification
import com.copperleaf.ballast.Queued
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import org.w3c.dom.HashChangeEvent
import org.w3c.dom.events.Event

private typealias HashInterceptorScope = BallastInterceptorScope<
    RouterContract.Inputs,
    RouterContract.Events,
    RouterContract.State,
    >

private typealias RouterNotifications = Flow<BallastNotification<
    RouterContract.Inputs,
    RouterContract.Events,
    RouterContract.State,
    >>

public class BrowserHashNavigationInterceptor : BallastInterceptor<
    RouterContract.Inputs,
    RouterContract.Events,
    RouterContract.State,
    > {

    override fun HashInterceptorScope.start(notifications: RouterNotifications) {
        launch(start = CoroutineStart.UNDISPATCHED) {
            // start by setting the initial route from the browser hash, if provided when the webpage first loads
            onViewModelInitSetStateFromBrowserHash(notifications)

            // then sync the hash state to the router state (in both directions)
            joinAll(
                updateBrowserHashOnStateChange(notifications),
                updateStateOnBrowserHashChange(notifications)
            )
        }
    }

    private fun HashInterceptorScope.updateBrowserHashOnStateChange(
        notifications: RouterNotifications
    ): Job = launch(start = CoroutineStart.UNDISPATCHED) {
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
                    window.location.hash = it?.path ?: ""
                }
                .launchIn(this)
        }
    }

    private fun HashInterceptorScope.updateStateOnBrowserHashChange(
        notifications: RouterNotifications
    ): Job = launch(start = CoroutineStart.UNDISPATCHED) {
        val hashChangeEventAsFlow = callbackFlow {
            val callback = { event: Event ->
                if (event is HashChangeEvent) {
                    console.log(event)
                    this@callbackFlow.trySend(event)
                }

                Unit
            }
            window.addEventListener("hashchange", callback)

            awaitClose {
                window.removeEventListener("hashchange", callback)
            }
        }

        hashChangeEventAsFlow
            .onEach { ev ->
                sendToQueue(
                    Queued.HandleInput(
                        null,
                        RouterContract.Inputs.GoToDestination(
                            destination = ev.newURL.split("#").last()
                        )
                    )
                )
            }
            .launchIn(this)
    }

    private suspend fun HashInterceptorScope.onViewModelInitSetStateFromBrowserHash(
        notifications: RouterNotifications
    ) {
        // wait for the BallastNotification.ViewModelStarted to be sent
        notifications
            .filterIsInstance<BallastNotification.ViewModelStarted<
                RouterContract.Inputs,
                RouterContract.Events,
                RouterContract.State,
                >>()
            .first()

        val currentHash = window.location.hash.trimStart('#')
        if (currentHash.isNotBlank()) {
            // initialize the VM with the current browser hash
            sendToQueue(
                Queued.HandleInput(
                    null,
                    RouterContract.Inputs.GoToDestination(
                        destination = currentHash
                    )
                )
            )

            // wait for the current hash to be set in the state, before allowing the rest of the hash-syncing to begin
            notifications
                .filterIsInstance<BallastNotification.StateChanged<
                    RouterContract.Inputs,
                    RouterContract.Events,
                    RouterContract.State,
                    >>()
                .first { it.state.currentDestination?.originalUrl == currentHash }
        }
    }
}
