package com.copperleaf.ballast.savedstate

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastInterceptorScope
import com.copperleaf.ballast.BallastNotification
import com.copperleaf.ballast.Queued
import com.copperleaf.ballast.internal.Status
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

public class BallastSavedStateInterceptor<Inputs : Any, Events : Any, State : Any>(
    private val adapter: SavedStateAdapter<Inputs, Events, State>,
    private val bufferFlow: (Flow<State>) -> Flow<State> = { it },
) : BallastInterceptor<Inputs, Events, State> {

    private var stateRestored: Boolean = false
    private var restorationJob: Job? = null
    private var previousState: State? = null
    private val statesChannel: Channel<State> = Channel(Channel.UNLIMITED)
    private var interceptorScope: BallastInterceptorScope<Inputs, Events, State>? = null

    override fun BallastInterceptorScope<Inputs, Events, State>.start(notifications: Flow<BallastNotification<Inputs, Events, State>>) {
        interceptorScope = this

        processNotifications(notifications)
        processStateChanges()
    }

    private fun BallastInterceptorScope<Inputs, Events, State>.processNotifications(notifications: Flow<BallastNotification<Inputs, Events, State>>) {
        launch(start = CoroutineStart.UNDISPATCHED) {
            notifications
                .collect {
                    when (it) {
                        is BallastNotification.ViewModelStatusChanged -> {
                            when (it.status) {
                                is Status.Running -> {
                                    requestStateRestoration()
                                }

                                else -> {
                                    // these other statuses are fine to be sent any time, we can ignore them
                                }
                            }
                        }

                        is BallastNotification.StateChanged<Inputs, Events, State> -> {
                            // send all state changes to the Channel
                            statesChannel.send(it.state)
                        }

                        is BallastNotification.EventProcessingStarted,
                        is BallastNotification.EventProcessingStopped,
                        is BallastNotification.InterceptorAttached,
                        is BallastNotification.InterceptorFailed,
                        is BallastNotification.UnhandledError -> {
                            // these notifications are fine to be sent any time, we can ignore them
                        }

                        else -> {
                            // any notifications about Inputs, Events, or SideJobs must wait until state has been
                            // restored at least once
                            check(stateRestored) { "Nothing can be processed until the state has been restored" }
                        }
                    }
                }
            interceptorScope = null
        }
    }

    private fun BallastInterceptorScope<Inputs, Events, State>.processStateChanges() {
        launch(start = CoroutineStart.UNDISPATCHED) {
            statesChannel
                .receiveAsFlow()
                .let { bufferFlow(it) }
                .onEach { nextState ->
                    if (stateRestored) {
                        val scope = SaveStateScopeImpl<Inputs, Events, State>(
                            interceptorScope = this@processStateChanges,
                            previousState = previousState,
                            nextState = nextState,
                        )
                        with(adapter) {
                            scope.save()
                        }
                    }

                    previousState = nextState
                }
                .launchIn(this)
        }
    }

    @Suppress("DEPRECATION")
    private fun BallastInterceptorScope<Inputs, Events, State>.requestStateRestoration() {
        val notNullScope = requireNotNull(interceptorScope) {
            "ViewModel with BallastSavedStateInterceptor is not started or has completed"
        }
        restorationJob?.cancel()
        restorationJob = with(notNullScope) {
            launch {
                val stateRestoredDeferred = CompletableDeferred<Unit>()

                val scope = RestoreStateScopeImpl<Inputs, Events, State>(
                    interceptorScope = this@requestStateRestoration,
                )
                val restoredState = with(adapter) {
                    scope.restore()
                }

                sendToQueue(
                    Queued.RestoreState(stateRestoredDeferred, restoredState)
                )
                stateRestoredDeferred.await()
                stateRestored = true

                scope.eventsToPostAfterRestore.forEach { postEvent(it) }
                scope.inputToPostAfterRestore.forEach { sendToQueue(Queued.HandleInput(null, it)) }
            }
        }
    }
}
