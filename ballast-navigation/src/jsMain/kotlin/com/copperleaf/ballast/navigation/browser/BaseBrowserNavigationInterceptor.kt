package com.copperleaf.ballast.navigation.browser

import com.copperleaf.ballast.Queued
import com.copperleaf.ballast.awaitViewModelStart
import com.copperleaf.ballast.events
import com.copperleaf.ballast.navigation.routing.Route
import com.copperleaf.ballast.navigation.routing.RouterContract
import com.copperleaf.ballast.navigation.routing.build
import com.copperleaf.ballast.navigation.routing.directions
import com.copperleaf.ballast.navigation.routing.mapCurrentDestination
import com.copperleaf.ballast.navigation.vm.RouterInterceptor
import com.copperleaf.ballast.navigation.vm.RouterInterceptorScope
import com.copperleaf.ballast.navigation.vm.RouterNotification
import io.ktor.http.Url
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

@Suppress("UNUSED_PARAMETER")
public abstract class BaseBrowserNavigationInterceptor<T : Route>(
    private val initialRoute: T
) : RouterInterceptor<T> {

    internal abstract fun getInitialUrl(): Url?
    internal abstract fun watchForUrlChanges(): Flow<Url>
    internal abstract fun setDestinationUrl(url: Url)

    final override fun RouterInterceptorScope<T>.start(
        notifications: Flow<RouterNotification<T>>
    ) {
        launch(start = CoroutineStart.UNDISPATCHED) {
            // start by setting the initial route from the browser hash, if provided when the webpage first loads
            onViewModelInitSetStateFromBrowser(notifications)

            // then sync the hash state to the router state (in both directions)
            joinAll(
                updateBrowserOnStateChange(notifications),
                updateStateOnBrowserChange(notifications)
            )
        }
    }

    private suspend fun RouterInterceptorScope<T>.onViewModelInitSetStateFromBrowser(
        notifications: Flow<RouterNotification<T>>
    ) {
        // wait for the BallastNotification.ViewModelStarted to be sent
        notifications.awaitViewModelStart()

        val initialDestinationUrl = getInitialUrl()?.encodedPathAndQuery
            ?: initialRoute.directions().build()

        val deferred = CompletableDeferred<Unit>()

        sendToQueue(
            Queued.HandleInput(
                deferred,
                RouterContract.Inputs.GoToDestination(
                    destination = initialDestinationUrl
                )
            )
        )

        // wait for the initial URL to be set in the state, before allowing the rest of the address bar syncing to begin
        deferred.await()
    }

    private fun RouterInterceptorScope<T>.updateBrowserOnStateChange(
        notifications: Flow<RouterNotification<T>>
    ): Job = launch(start = CoroutineStart.UNDISPATCHED) {
        notifications
            .events { it }
            .filterIsInstance<RouterContract.Events.BackstackChanged<T>>()
            .mapNotNull { ev ->
                ev.backstack.mapCurrentDestination(
                    route = {
                        if (annotations.any { it is WebEventRouteAnnotation }) {
                            // ignore this request
                            null
                        } else {
                            Url(originalDestinationUrl)
                        }
                    },
                    notFound = { Url(it) },
                )
            }
            .distinctUntilChanged()
            .onEach { destination -> setDestinationUrl(destination) }
            .launchIn(this)
    }

    private fun RouterInterceptorScope<T>.updateStateOnBrowserChange(
        notifications: Flow<RouterNotification<T>>
    ): Job = launch(start = CoroutineStart.UNDISPATCHED) {
        watchForUrlChanges()
            .onEach { destination ->
                sendToQueue(
                    Queued.HandleInput(
                        null,
                        RouterContract.Inputs.GoToDestination(
                            destination = destination.encodedPathAndQuery,
                            extraAnnotations = setOf(WebEventRouteAnnotation),
                        )
                    )
                )
            }
            .launchIn(this)
    }
}
