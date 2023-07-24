package com.copperleaf.ballast.internal.actors

import com.copperleaf.ballast.BallastNotification
import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.internal.BallastViewModelImpl
import com.copperleaf.ballast.internal.Status
import com.copperleaf.ballast.internal.scopes.EventHandlerScopeImpl
import com.copperleaf.ballast.internal.scopes.EventStrategyScopeImpl
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

internal class EventActor<Inputs : Any, Events : Any, State : Any>(
    private val impl: BallastViewModelImpl<Inputs, Events, State>
) {

    public fun attachEventHandler(
        handler: EventHandler<Inputs, Events, State>,
        coroutineScope: CoroutineScope,
    ) {
        val eventHandlerCoroutineScope = coroutineScope +
                impl.coordinator.uncaughtExceptionHandler +
                impl.eventsDispatcher

        eventHandlerCoroutineScope.launch {
            impl.interceptorActor.notify(BallastNotification.EventProcessingStarted(impl.type, impl.name))

            coroutineContext.job.invokeOnCompletion {
                impl.interceptorActor.notifyImmediate(BallastNotification.EventProcessingStopped(impl.type, impl.name))
            }

            val eventStrategyScope = EventStrategyScopeImpl(impl, impl.eventActor, handler)

            with(impl.eventStrategy) {
                eventStrategyScope.start()
            }
        }
    }

    internal suspend fun enqueueEvent(event: Events, deferred: CompletableDeferred<Unit>?, await: Boolean) {
        impl.coordinator.coordinatorState.value.checkEventsOpen()
        impl.interceptorActor.notify(BallastNotification.EventQueued(impl.type, impl.name, event))
        impl.eventStrategy.enqueue(event)
        if (await && deferred != null) {
            deferred.await()
        }
    }

    internal suspend fun safelyHandleEvent(
        event: Events,
        handler: EventHandler<Inputs, Events, State>
    ) {
        impl.interceptorActor.notify(BallastNotification.EventEmitted(impl.type, impl.name, event))
        try {
            coroutineScope {
                val handlerScope = EventHandlerScopeImpl(impl, impl.inputActor, impl.interceptorActor)
                with(handler) {
                    handlerScope.handleEvent(event)
                }
                handlerScope.ensureUsedCorrectly()
                impl.interceptorActor.notify(BallastNotification.EventHandledSuccessfully(impl.type, impl.name, event))
            }
        } catch (e: CancellationException) {
            // ignore
        } catch (e: Throwable) {
            impl.interceptorActor.notify(BallastNotification.EventHandlerError(impl.type, impl.name, event, e))
        }
    }

    internal suspend fun gracefullyShutDownEvents() {
        impl.coordinator.coordinatorState.update {
            Status.ShuttingDown(
                stateChangeOpen = false,
                mainQueueOpen = false,
                eventsOpen = false,
                sideJobsOpen = false,
                sideJobsCancellationOpen = false,
            )
        }
        impl.interceptorActor.notify(
            BallastNotification.ViewModelStatusChanged(
                impl.type,
                impl.name,
                impl.coordinator.coordinatorState.value
            )
        )

        // close the Events queue and wait for all Events to be handled
        impl.eventStrategy.close()
        impl.eventStrategy.flush()
    }
}
