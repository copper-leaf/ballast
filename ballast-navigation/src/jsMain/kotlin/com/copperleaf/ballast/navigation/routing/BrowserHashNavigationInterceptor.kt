package com.copperleaf.ballast.navigation.routing

import com.copperleaf.ballast.BallastNotification
import com.copperleaf.ballast.Queued
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
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

public class BrowserHashNavigationInterceptor : RouterInterceptor {

    override fun RouterInterceptorScope.start(notifications: RouterNotifications) {
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

    private fun RouterInterceptorScope.updateBrowserHashOnStateChange(
        notifications: RouterNotifications
    ): Job = launch(start = CoroutineStart.UNDISPATCHED) {
        launch(start = CoroutineStart.UNDISPATCHED) {
            notifications
                .filterIsInstance<BallastNotification.StateChanged<
                    RouterContract.Inputs,
                    RouterContract.Events,
                    RouterContract.State,
                    >>()
                .map {
                    when (val destination = it.state.currentDestinationOrNotFound) {
                        is Destination -> destination.path
                        is MissingDestination -> destination.originalUrl
                        else -> null
                    }
                }
                .distinctUntilChanged()
                .onEach {
                    window.location.hash = it ?: ""
                }
                .launchIn(this)
        }
    }

    private fun RouterInterceptorScope.updateStateOnBrowserHashChange(
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

    private suspend fun RouterInterceptorScope.onViewModelInitSetStateFromBrowserHash(
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
