package com.copperleaf.ballast.internal.actors

import com.copperleaf.ballast.BallastNotification
import com.copperleaf.ballast.SideJobScope
import com.copperleaf.ballast.internal.BallastViewModelImpl
import com.copperleaf.ballast.internal.Status
import com.copperleaf.ballast.internal.scopes.SideJobScopeImpl
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration

internal class SideJobActor<Inputs : Any, Events : Any, State : Any>(
    private val impl: BallastViewModelImpl<Inputs, Events, State>
) {
    private val _sideJobsRequestQueue: Channel<SideJobRequest<Inputs, Events, State>> =
        Channel(BUFFERED, BufferOverflow.SUSPEND)
    private val _sideJobsRequestQueueDrained = CompletableDeferred<Unit>()

    private val sideJobsState: MutableStateFlow<Map<String, SideJobList>> = MutableStateFlow(
        emptyMap(),
    )

    internal fun startSideJobsInternal() {
        // start sideJobs posted by Inputs
        impl.viewModelScope.launch {
            _sideJobsRequestQueue
                .receiveAsFlow()
                .onEach { request ->
                    when (request) {
                        is SideJobRequest.StartOrRestartSideJob -> {
                            safelyStartOrRestartSideJob(request)
                        }

                        is SideJobRequest.CancelSideJob -> {
                            safelyCancelSideJob(request)
                        }
                    }
                }
                .onCompletion { _sideJobsRequestQueueDrained.complete(Unit) }
                .launchIn(this)
        }
    }

    internal fun enqueueSideJob(
        key: String,
        block: suspend SideJobScope<Inputs, Events, State>.() -> Unit,
    ) {
        impl.coordinator.coordinatorState.value.checkSideJobsOpen()
        impl.interceptorActor.notifyImmediate(BallastNotification.SideJobQueued(impl.type, impl.name, key))
        _sideJobsRequestQueue.trySend(SideJobRequest.StartOrRestartSideJob(key, block))
    }

    internal fun cancelSideJob(
        key: String,
    ) {
        impl.coordinator.coordinatorState.value.checkSideJobCancellationOpen()
        _sideJobsRequestQueue.trySend(SideJobRequest.CancelSideJob(key))
    }

    internal fun cancelAllSideJobs() {
        sideJobsState.getAndUpdate {
            emptyMap()
        }
    }

    private fun safelyStartOrRestartSideJob(
        request: SideJobRequest.StartOrRestartSideJob<Inputs, Events, State>,
    ) {
        val newState = sideJobsState.updateAndGet {
            val sideJobMap = it.toMutableMap()

            val sideJobListForKey = sideJobMap.getOrPut(request.key) { SideJobList(request.key) }
            val updatedList = sideJobListForKey.addNewRunningJob(impl.viewModelScope.coroutineContext.job)
            sideJobMap[request.key] = updatedList

            sideJobMap.toMap()
        }

        val sideJobListForKey = newState[request.key]!!
        val latestSideJobForKey = sideJobListForKey.currentSideJob!!

        val sideJobCoroutineScope = impl.viewModelScope +
                latestSideJobForKey.job +
                impl.sideJobsDispatcher

        sideJobCoroutineScope.launch {
            impl.interceptorActor.notify(
                BallastNotification.SideJobStarted(
                    impl.type,
                    impl.name,
                    key = latestSideJobForKey.key,
                    restartState = latestSideJobForKey.restartState,
                )
            )

            try {
                // run the sideJob, which may never complete
                coroutineScope {
                    val sideJobScope = SideJobScopeImpl(
                        sideJobCoroutineScope = this,
                        logger = impl.logger,

                        inputActor = impl.inputActor,
                        eventActor = impl.eventActor,
                        interceptorActor = impl.interceptorActor,

                        key = latestSideJobForKey.key,
                        restartState = latestSideJobForKey.restartState,
                    )
                    request.block.invoke(sideJobScope)
                }

                // If it does complete normally...

                // mark the sideJob as complete
                completeSideJob(latestSideJobForKey.key, latestSideJobForKey.sideJobId)

                // send a notification that the side job has completed
                impl.interceptorActor.notify(
                    BallastNotification.SideJobCompleted(
                        impl.type,
                        impl.name,
                        key = latestSideJobForKey.key,
                        restartState = latestSideJobForKey.restartState,
                    )
                )
            } catch (e: CancellationException) {
                // The side job was cancelled, either by being restarted, manually cancelled, or because the whole
                // ViewModel was cancelled.

                // mark the sideJob as complete
                completeSideJob(latestSideJobForKey.key, latestSideJobForKey.sideJobId)

                // send a notification that the side job was cancelled
                impl.interceptorActor.notify(
                    BallastNotification.SideJobCancelled(
                        impl.type,
                        impl.name,
                        key = latestSideJobForKey.key,
                        restartState = latestSideJobForKey.restartState,
                    )
                )
            } catch (e: Throwable) {
                // an exception was thrown when executing the sideJob.

                // mark the sideJob as complete
                completeSideJob(latestSideJobForKey.key, latestSideJobForKey.sideJobId)

                // send a notification that the side job was cancelled
                impl.interceptorActor.notify(
                    BallastNotification.SideJobError(
                        impl.type,
                        impl.name,
                        key = latestSideJobForKey.key,
                        restartState = latestSideJobForKey.restartState,
                        e,
                    )
                )
            }
        }
    }

    private fun safelyCancelSideJob(
        request: SideJobRequest.CancelSideJob<Inputs, Events, State>,
    ) {
        sideJobsState.value[request.key]?.cancelSideJobs()
    }

    private fun safelyCancelAllSideJobs() {
        sideJobsState.value.forEach { it.value.cancelSideJobs() }
    }

    private fun completeSideJob(
        key: String,
        id: Int,
    ) {
        sideJobsState.updateAndGet {
            val sideJobMap = it.toMutableMap()

            val sideJobListForKey = sideJobMap.getOrPut(key) { SideJobList(key) }
            val updatedList = sideJobListForKey.removeCompletedJob(id)
            sideJobMap[key] = updatedList

            sideJobMap.toMap()
        }
    }

    internal suspend fun gracefullyShutDownSideJobs(gracePeriod: Duration) {
        impl.coordinator.coordinatorState.update {
            Status.ShuttingDown(
                stateChangeOpen = true,
                mainQueueOpen = true,
                eventsOpen = true,
                sideJobsOpen = false,
                sideJobsCancellationOpen = true,
            )
        }
        impl.interceptorActor.notify(
            BallastNotification.ViewModelStatusChanged(
                impl.type,
                impl.name,
                impl.coordinator.coordinatorState.value
            )
        )

        try {
            withTimeout(gracePeriod) {
                // close the sideJobs request queue and wait for all requests to be handled
                _sideJobsRequestQueue.close()
                _sideJobsRequestQueueDrained.await()

                // without forcibly cancelling, wait for all sideJobs to complete
                sideJobsState.value
                    .flatMap { it.value.runningJobs.map { it.job } }
                    .joinAll()
            }
        } catch (e: Exception) {
            // the sideJobs did not complete during the grace period. Force them all to cancel, then wait for them to
            // complete
            safelyCancelAllSideJobs()
            sideJobsState.value
                .flatMap { it.value.runningJobs.map { it.job } }
                .joinAll()
        }
    }

    internal fun close() {
        _sideJobsRequestQueue.close()
    }

    private sealed class SideJobRequest<Inputs : Any, Events : Any, State : Any> {
        internal class StartOrRestartSideJob<Inputs : Any, Events : Any, State : Any>(
            val key: String,
            val block: suspend SideJobScope<Inputs, Events, State>.() -> Unit,
        ) : SideJobRequest<Inputs, Events, State>()

        internal class CancelSideJob<Inputs : Any, Events : Any, State : Any>(
            val key: String,
        ) : SideJobRequest<Inputs, Events, State>()
    }

    private class RunningSideJob(
        internal val sideJobId: Int,
        internal val key: String,
        internal var restartState: SideJobScope.RestartState,
        internal var job: Job,
    )

    private data class SideJobList(
        internal val key: String,
        internal val autoIncrement: Int = 1,
        internal val runningJobs: List<RunningSideJob> = emptyList(),
    ) {
        val currentSideJob: RunningSideJob? = runningJobs.lastOrNull()

        fun addNewRunningJob(
            parentJob: Job
        ): SideJobList {
            val restartState = if (autoIncrement > 1) {
                // if there are any active jobs at this key, we are restarting this sideJob
                SideJobScope.RestartState.Restarted
            } else {
                // if the list was empty or all previous sideJobs at this key have completed, then we are starting it afresh
                SideJobScope.RestartState.Initial
            }

            // make sure all runningJobs are cancelled, each key can only have 1 active sideJob at a time
            cancelSideJobs()

            val newSideJob = RunningSideJob(
                sideJobId = this.autoIncrement,
                key = this.key,
                restartState = restartState,
                job = SupervisorJob(parent = parentJob),
            )

            return copy(
                autoIncrement = this.autoIncrement + 1,
                runningJobs = this.runningJobs + newSideJob
            )
        }

        fun cancelSideJobs() {
            runningJobs.forEach { it.job.cancel() }
        }

        fun removeCompletedJob(id: Int): SideJobList {
            val sideJobAtKey = runningJobs.singleOrNull { it.sideJobId == id }

            if (sideJobAtKey != null) {
                // cancel the sideJob's coroutine, if needed
                sideJobAtKey.job.cancel()
                return copy(
                    runningJobs = this.runningJobs - sideJobAtKey
                )
            } else {
                return copy()
            }
        }
    }
}
