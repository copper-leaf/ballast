package com.copperleaf.ballast.internal.actors

import com.copperleaf.ballast.BallastNotification
import com.copperleaf.ballast.internal.BallastViewModelImpl
import com.copperleaf.ballast.internal.Status
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlin.time.Duration

internal class ActorCoordinator<Inputs : Any, Events : Any, State : Any>(
    private val impl: BallastViewModelImpl<Inputs, Events, State>
) {

    internal val coordinatorState: MutableStateFlow<Status> = MutableStateFlow(
        Status.NotStarted
    )

    internal val uncaughtExceptionHandler = CoroutineExceptionHandler { _, e ->
        impl.interceptorActor.notifyImmediate(BallastNotification.UnhandledError(impl.type, impl.name, e))
    }

    public fun start(coroutineScope: CoroutineScope) {
        // check the ViewModel is in a valid state to be started
        coordinatorState.value.checkCanStart()

        // create the real viewModel's coroutineScope
        impl.viewModelScope = coroutineScope +
                uncaughtExceptionHandler +
                SupervisorJob(parent = coroutineScope.coroutineContext.job)

        // set the VM to clear itself upon the cancellation of its coroutine scope
        impl.viewModelScope.coroutineContext.job.invokeOnCompletion {
            onCleared()
        }

        // launch a job to initiate the startup sequence for this VM
        impl.viewModelScope.launch(start = CoroutineStart.UNDISPATCHED) {
            // mark the VM as being in the Running state. This is only checked internally, not sent to Interceptors yet
            coordinatorState.update {
                Status.Running
            }

            // start processing the main queue, accepting Inputs for processing through the InputStrategy
            impl.inputActor.startMainQueueInternal()

            // start processing the sideJobs queue, so requests for sideJobs from Inputs will be started
            impl.sideJobActor.startSideJobsInternal()

            // let the interceptors start running, each on their own isolated coroutineScopes that are a child of the
            // main viewModelScope
            impl.interceptorActor.startInterceptorsInternal()

            // now that the Interceptors are running, we can start processing any Notifications that should be sent to
            // them. Any Notifications emitted before this line will just be buffered so that Interceptors do not miss
            // any notifications that were sent before they launched
            impl.interceptorActor.startProcessingNotificationsInternal()

            // notify interceptors that the VM is officially in the Running state. Many Interceptors will wait for this
            // explicit signal before doing any further processing.
            impl.interceptorActor.notify(
                BallastNotification.ViewModelStatusChanged(
                    impl.type,
                    impl.name,
                    coordinatorState.value
                )
            )

            // emit the initial state to all Interceptors
            impl.interceptorActor.notify(BallastNotification.StateChanged(impl.type, impl.name, impl.initialState))
        }
    }

    private fun onCleared() {
        coordinatorState.value.checkCanClear()

        // side-jobs are already bound by the viewModelScope and will get cancelled automatically, but we still need
        // to clear the internal state
        coordinatorState.getAndUpdate {
            Status.Cleared
        }
        impl.sideJobActor.cancelAllSideJobs()

        // send the final notification to Interceptors that the status has changed to Cleared
        impl.interceptorActor.notifyImmediate(
            BallastNotification.ViewModelStatusChanged(
                impl.type,
                impl.name,
                coordinatorState.value
            )
        )

        // ensure all queues are closed. In a graceful shutdown they will be closed already, but if the VM was closed by
        // the cancellation of its coroutineScope, then they will not be closed until this point.
        impl.inputStrategy.close()
        impl.eventStrategy.close()
        impl.sideJobActor.close()
        impl.interceptorActor.close()
    }

    internal fun gracefullyShutDown(gracePeriod: Duration, deferred: CompletableDeferred<Unit>?) {
        coordinatorState.value.checkCanShutDown()
        impl.viewModelScope.launch {
            coordinatorState.update {
                Status.ShuttingDown(
                    stateChangeOpen = true,
                    mainQueueOpen = true,
                    eventsOpen = true,
                    sideJobsOpen = true,
                    sideJobsCancellationOpen = true,
                )
            }

            // notify Interceptors that we are now starting to shut down the VM. This process may take some time
            impl.interceptorActor.notify(
                BallastNotification.ViewModelStatusChanged(
                    impl.type,
                    impl.name,
                    coordinatorState.value
                )
            )

            // first shut down the sideJobs
            impl.sideJobActor.gracefullyShutDownSideJobs(gracePeriod)

            // then, shut down the main queue, preventing any more Inputs from being received
            impl.inputActor.gracefullyShutDownMainQueue()

            // then, drain the events channel
            impl.eventActor.gracefullyShutDownEvents()

            // finally, drain the notifications channel
            impl.interceptorActor.gracefullyShutDownNotifications()

            deferred?.complete(Unit)

            // There should be no data left flowing throughout the VM at this point. Cancel its own viewModelScope to
            // make sure nothing else will start processing
            impl.viewModelScope.cancel()
        }
    }
}
