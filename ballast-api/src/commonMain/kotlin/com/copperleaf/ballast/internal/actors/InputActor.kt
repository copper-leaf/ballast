package com.copperleaf.ballast.internal.actors

import com.copperleaf.ballast.BallastNotification
import com.copperleaf.ballast.InputStrategy
import com.copperleaf.ballast.Queued
import com.copperleaf.ballast.internal.BallastViewModelImpl
import com.copperleaf.ballast.internal.Status
import com.copperleaf.ballast.internal.scopes.InputHandlerScopeImpl
import com.copperleaf.ballast.internal.scopes.InputStrategyScopeImpl
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.ChannelResult
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.plus

internal class InputActor<Inputs : Any, Events : Any, State : Any>(
    private val impl: BallastViewModelImpl<Inputs, Events, State>
) {

    internal fun startMainQueueInternal() {
        // observe and process Inputs
        val scope = InputStrategyScopeImpl(
            impl.viewModelScope + impl.inputsDispatcher,
            impl,
            impl.inputActor,
            impl.stateActor,
        )
        with(impl.inputStrategy) {
            scope.start()
        }
    }

    internal suspend fun enqueueQueued(queued: Queued<Inputs, Events, State>, await: Boolean) {
        impl.coordinator.coordinatorState.value.checkMainQueueOpen()

        when (queued) {
            is Queued.HandleInput -> {
                impl.interceptorActor.notify(BallastNotification.InputQueued(impl.type, impl.name, queued.input))
            }

            is Queued.RestoreState -> {

            }

            is Queued.ShutDownGracefully -> {

            }
        }

        impl.inputStrategy.enqueue(queued)

        if (await) {
            queued.deferred?.await()
        }
    }

    internal fun enqueueQueuedImmediate(queued: Queued<Inputs, Events, State>): ChannelResult<Unit> {
        impl.coordinator.coordinatorState.value.checkMainQueueOpen()

        when (queued) {
            is Queued.HandleInput -> {
                impl.interceptorActor.notifyImmediate(BallastNotification.InputQueued(impl.type, impl.name, queued.input))
            }

            is Queued.RestoreState -> {

            }

            is Queued.ShutDownGracefully -> {

            }
        }

        val result = impl.inputStrategy.tryEnqueue(queued)

        if (result.isFailure || result.isClosed) {
            when (queued) {
                is Queued.HandleInput -> {
                    impl.interceptorActor.notifyImmediate(BallastNotification.InputDropped(impl.type, impl.name, queued.input))
                }

                is Queued.RestoreState -> {

                }

                is Queued.ShutDownGracefully -> {

                }
            }
        }
        return result
    }

    internal suspend fun safelyHandleQueued(
        queued: Queued<Inputs, Events, State>,
        guardian: InputStrategy.Guardian,
        onCancelled: suspend () -> Unit
    ) {
        when (queued) {
            is Queued.HandleInput -> {
                safelyHandleInput(queued.input, queued.deferred, guardian, onCancelled)
            }

            is Queued.RestoreState -> {
                impl.stateActor.safelySetState(queued.state, queued.deferred)
            }

            is Queued.ShutDownGracefully -> {
                impl.coordinator.gracefullyShutDown(queued.gracePeriod, queued.deferred)
            }
        }
    }

    private suspend fun safelyHandleInput(
        input: Inputs,
        deferred: CompletableDeferred<Unit>?,
        guardian: InputStrategy.Guardian,
        onCancelled: suspend () -> Unit
    ) {
        impl.interceptorActor.notify(BallastNotification.InputAccepted(impl.type, impl.name, input))

        try {
            coroutineScope {
                // Create a handler scope to handle the input normally
                val handlerScope =
                    InputHandlerScopeImpl(guardian, impl, impl.stateActor, impl.eventActor, impl.sideJobActor)
                with(impl.inputHandler) {
                    handlerScope.handleInput(input)
                }
                handlerScope.close()

                try {
                    impl.interceptorActor.notify(BallastNotification.InputHandledSuccessfully(impl.type, impl.name, input))
                } catch (t: Throwable) {
                    impl.interceptorActor.notify(BallastNotification.InputHandlerError(impl.type, impl.name, input, t))
                }
                deferred?.complete(Unit)
            }
        } catch (e: CancellationException) {
            // when the coroutine is cancelled for any reason, we must assume the input did not
            // complete and may have left the State in a bad, erm..., state. We should reset it and
            // try to forget that we ever tried to process it in the first place
            impl.interceptorActor.notify(BallastNotification.InputCancelled(impl.type, impl.name, input))
            onCancelled()
            deferred?.complete(Unit)
        } catch (e: Throwable) {
            impl.interceptorActor.notify(BallastNotification.InputHandlerError(impl.type, impl.name, input, e))
            deferred?.complete(Unit)
        }
    }

    internal suspend fun gracefullyShutDownMainQueue() {
        impl.coordinator.coordinatorState.update {
            Status.ShuttingDown(
                stateChangeOpen = true,
                mainQueueOpen = false,
                eventsOpen = true,
                sideJobsOpen = false,
                sideJobsCancellationOpen = true,
            )
        }
        impl.interceptorActor.notify(BallastNotification.ViewModelStatusChanged(impl.type, impl.name, impl.coordinator.coordinatorState.value))

        // close the main queue and wait for all Inputs to be handled
        impl.inputStrategy.close()
        impl.inputStrategy.flush()
    }
}
