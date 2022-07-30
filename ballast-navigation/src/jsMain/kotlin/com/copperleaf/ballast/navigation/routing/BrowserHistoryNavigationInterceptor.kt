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
import org.w3c.dom.PopStateEvent
import org.w3c.dom.url.URL

public class BrowserHistoryNavigationInterceptor(
    private val baseUrl: String
) : RouterInterceptor {

    override fun RouterInterceptorScope.start(notifications: RouterNotifications) {
        launch(start = CoroutineStart.UNDISPATCHED) {
            // start by setting the initial route from the browser hash, if provided when the webpage first loads
            onViewModelInitSetStateFromBrowserHistory(notifications)

            // then sync the hash state to the router state (in both directions)
            joinAll(
                updateBrowserHistoryOnStateChange(notifications),
                updateStateOnBrowserHistoryChange(notifications)
            )
        }
    }

    private fun RouterInterceptorScope.updateBrowserHistoryOnStateChange(
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
                    val updatedPath = it ?: ""

                    if(getCurrentPath() != updatedPath) {
                        window.history.pushState(it ?: "", "", it ?: "")
                    }
                }
                .launchIn(this)
        }
    }

    private fun RouterInterceptorScope.updateStateOnBrowserHistoryChange(
        notifications: RouterNotifications
    ): Job = launch(start = CoroutineStart.UNDISPATCHED) {
        val popStateEventAsFlow = callbackFlow {
            val callback = { event: PopStateEvent ->
                this@callbackFlow.trySend(event)

                Unit
            }
            window.onpopstate = callback

            awaitClose {
                window.onpopstate = null
            }
        }

        popStateEventAsFlow
            .onEach { ev ->
                sendToQueue(
                    Queued.HandleInput(
                        null,
                        RouterContract.Inputs.GoToDestination(
                            destination = ev.state.toString()
                        )
                    )
                )
            }
            .launchIn(this)
    }

    private suspend fun RouterInterceptorScope.onViewModelInitSetStateFromBrowserHistory(
        notifications: RouterNotifications
    ) {
        // wait for the BallastNotification.ViewModelStarted to be sent
        console.log("waiting for VM Started event")

        notifications
            .filterIsInstance<BallastNotification.ViewModelStarted<
                RouterContract.Inputs,
                RouterContract.Events,
                RouterContract.State,
                >>()
            .first()

        val currentPath = getCurrentPath()
        if (currentPath.isNotBlank()) {
            console.log("loading current path into router")

            // initialize the VM with the current browser hash
            sendToQueue(
                Queued.HandleInput(
                    null,
                    RouterContract.Inputs.GoToDestination(
                        destination = "/$currentPath"
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
                .first { it.state.currentDestination?.originalUrl == currentPath }
        }
    }

    private fun getCurrentPath(): String {
        val jsUrl = URL(baseUrl)
        val baseUrlPath = jsUrl.pathname.trimStart('/')

        val currentPath = window.location.pathname.trimStart('/').removePrefix(baseUrlPath).trimStart('/')
        console.log("baseUrlPath=$baseUrlPath")
        console.log("browserPath=${window.location.pathname}")
        console.log("currentPath=${currentPath}")
        return currentPath
    }
}


//        launch(start = CoroutineStart.UNDISPATCHED) {
//            notifications
//                .filterIsInstance<BallastNotification.StateChanged<
//                    RouterContract.Inputs,
//                    RouterContract.Events,
//                    RouterContract.State,
//                    >>()
//                .map { it.state.currentDestination }
//                .distinctUntilChanged()
//                .onEach {
//                    window.history.pushState(null, "", it?.path)
//                }
//                .launchIn(this)
//        }

//    private fun onPopStateAsFlow(): Flow<PopStateEvent> {
//        return callbackFlow {
//            val callback = { event: PopStateEvent ->
//                this@callbackFlow.trySend(event)
//
//                Unit
//            }
//            window.onpopstate = callback
//
//            awaitClose {
//                window.onpopstate = null
//            }
//        }
//    }
//}
