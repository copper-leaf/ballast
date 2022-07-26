package com.copperleaf.ballast.navigation.routing

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastInterceptorScope
import com.copperleaf.ballast.BallastNotification
import com.copperleaf.ballast.Queued
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
import org.w3c.dom.HashChangeEvent
import org.w3c.dom.events.Event

public class BrowserHashNavigationInterceptor : BallastInterceptor<
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
                    window.location.hash = it?.path ?: ""
                }
                .launchIn(this)
        }
        launch(start = CoroutineStart.UNDISPATCHED) {
            hashChangeEventAsFlow()
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
    }

    private fun hashChangeEventAsFlow(): Flow<HashChangeEvent> {
        return callbackFlow {
            val callback = { event: Event ->
                if(event is HashChangeEvent) {
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
    }
}
/*
"ClassCastException
    at THROW_CCE (webpack-internal:///./kotlin/kotlin-kotlin-stdlib-js-ir.js:25126:11)
    at goBack (webpack-internal:///./kotlin/ballast-ballast-navigation-js-ir.js:1798:9)
    at ReplaceTopDestination.updateBackstack_yekx37_k$ (webpack-internal:///./kotlin/ballast-ballast-navigation-js-ir.js:790:21)
    at eval (webpack-internal:///./kotlin/ballast-ballast-navigation-js-ir.js:997:21)
    at InputHandlerScopeImpl.updateStateAndGet_9wmo37_k$ (webpack-internal:///./kotlin/ballast-ballast-core-js-ir.js:4727:25)
    at $updateBackstackAndSendNotificationsCOROUTINE$0.doResume_5yljmg_k$ (webpack-internal:///./kotlin/ballast-ballast-navigation-js-ir.js:1024:51)
    at updateBackstackAndSendNotifications (webpack-internal:///./kotlin/ballast-ballast-navigation-js-ir.js:993:16)
    at RouterInputHandler.handleInput_7jn3py_k$ (webpack-internal:///./kotlin/ballast-ballast-navigation-js-ir.js:1089:12)
    at RouterInputHandler.handleInput_277jbq_k$ (webpack-internal:///./kotlin/ballast-ballast-navigation-js-ir.js:1092:17)
    at BallastViewModelImpl$safelyHandleInput$slambda.doResume_5yljmg_k$ (webpack-internal:///./kotlin/ballast-ballast-core-js-ir.js:3735:48)"
 */
