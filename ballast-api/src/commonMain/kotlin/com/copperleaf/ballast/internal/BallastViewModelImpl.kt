package com.copperleaf.ballast.internal

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastNotification
import com.copperleaf.ballast.BallastViewModel
import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.InputStrategy
import com.copperleaf.ballast.Queued
import com.copperleaf.ballast.SideJobScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.channels.ChannelResult
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration

public class BallastViewModelImpl<Inputs : Any, Events : Any, State : Any>(
    internal val type: String,
    config: BallastViewModelConfiguration<Inputs, Events, State>,
) : BallastViewModel<Inputs, Events, State>,
    BallastViewModelConfiguration<Inputs, Events, State> by config {

    private data class InternalState(
        val status: Status = Status.NotStarted,
        val sideJobs: Map<String, SideJobList> = emptyMap(),
    )

// Internal properties
// ---------------------------------------------------------------------------------------------------------------------

    private val _internalState: MutableStateFlow<InternalState> = MutableStateFlow(InternalState())
    private val _state: MutableStateFlow<State> = MutableStateFlow(initialState)

    public lateinit var viewModelScope: CoroutineScope

    private val uncaughtExceptionHandler = CoroutineExceptionHandler { _, e ->
        notifyImmediate(BallastNotification.UnhandledError(type, name, e))
    }

// Core MVI pattern API
// ---------------------------------------------------------------------------------------------------------------------

    override fun observeStates(): StateFlow<State> {
        return _state.asStateFlow()
    }

    override suspend fun send(element: Inputs) {
        enqueueQueued(Queued.HandleInput(null, element), await = false)
    }

    override suspend fun sendAndAwaitCompletion(element: Inputs) {
        enqueueQueued(Queued.HandleInput(CompletableDeferred(), element), await = true)
    }

    override fun trySend(element: Inputs): ChannelResult<Unit> {
        return enqueueQueuedImmediate(Queued.HandleInput(null, element))
    }

// ViewModel Lifecycle
// ---------------------------------------------------------------------------------------------------------------------

    public fun start(coroutineScope: CoroutineScope) {
        // check the ViewModel is in a valid state to be started
        _internalState.value.status.checkCanStart()

        // create the real viewModel's coroutineScope
        viewModelScope = coroutineScope +
                uncaughtExceptionHandler +
                SupervisorJob(parent = coroutineScope.coroutineContext.job)

        // set the VM to clear itself upon the cancellation of its coroutine scope
        viewModelScope.coroutineContext.job.invokeOnCompletion {
            onCleared()
        }

        // launch a job to initiate the startup sequence for this VM
        viewModelScope.launch(start = CoroutineStart.UNDISPATCHED) {
            // mark the VM as being in the Running state. This is only checked internally, not sent to Interceptors yet
            _internalState.update {
                it.copy(status = Status.Running)
            }

            // start processing the main queue, accepting Inputs for processing through the InputStrategy
            startMainQueueInternal()

            // start processing the sideJobs queue, so requests for sideJobs from Inputs will be started
            startSideJobsInternal()

            // let the interceptors start running, each on their own isolated coroutineScopes that are a child of the
            // main viewModelScope
            startInterceptorsInternal()

            // now that the Interceptors are running, we can start processing any Notifications that should be sent to
            // them. Any Notifications emitted before this line will just be buffered so that Interceptors do not miss
            // any notifications that were sent before they launched
            startProcessingNotificationsInternal()

            // notify interceptors that the VM is officially in the Running state. Many Interceptors will wait for this
            // explicit signal before doing any further processing.
            notify(BallastNotification.ViewModelStatusChanged(type, name, _internalState.value.status))

            // emit the initial state to all Interceptors
            notify(BallastNotification.StateChanged(type, name, initialState))
        }
    }

    private fun onCleared() {
        _internalState.value.status.checkCanClear()

        // side-jobs are already bound by the viewModelScope and will get cancelled automatically, but we still need
        // to clear the internal state
        _internalState.getAndUpdate {
            it.copy(status = Status.Cleared, sideJobs = emptyMap())
        }

        // send the final notification to Interceptors that the status has changed to Cleared
        notifyImmediate(BallastNotification.ViewModelStatusChanged(type, name, _internalState.value.status))

        // ensure all queues are closed. In a graceful shutdown they will be closed already, but if the VM was closed by
        // the cancellation of its coroutineScope, then they will not be closed until this point.
        inputStrategy.close()
        eventStrategy.close()
        _sideJobsRequestQueue.close()
        _notificationsQueue.close()
    }

// Internals
// ---------------------------------------------------------------------------------------------------------------------

    private fun gracefullyShutDown(gracePeriod: Duration, deferred: CompletableDeferred<Unit>?) {
        _internalState.value.status.checkCanShutDown()
        viewModelScope.launch {
            _internalState.update {
                it.copy(
                    status = Status.ShuttingDown(
                        stateChangeOpen = true,
                        mainQueueOpen = true,
                        eventsOpen = true,
                        sideJobsOpen = true,
                        sideJobsCancellationOpen = true,
                    )
                )
            }

            // notify Interceptors that we are now starting to shut down the VM. This process may take some time
            notify(BallastNotification.ViewModelStatusChanged(type, name, _internalState.value.status))

            // first shut down the sideJobs
            gracefullyShutDownSideJobs(gracePeriod)

            // then, shut down the main queue, preventing any more Inputs from being received
            gracefullyShutDownMainQueue()

            // then, drain the events channel
            gracefullyShutDownEvents()

            // finally, drain the notifications channel
            gracefullyShutDownNotifications()

            deferred?.complete(Unit)

            // There should be no data left flowing throughout the VM at this point. Cancel its own viewModelScope to
            // make sure nothing else will start processing
            viewModelScope.cancel()
        }
    }

// Main Queue
// ---------------------------------------------------------------------------------------------------------------------

    private fun startMainQueueInternal() {
        // observe and process Inputs
        val scope = InputStrategyScopeImpl(
            viewModelScope + inputsDispatcher,
            this@BallastViewModelImpl
        )
        with(inputStrategy) {
            scope.start()
        }
    }

    internal suspend fun enqueueQueued(queued: Queued<Inputs, Events, State>, await: Boolean) {
        _internalState.value.status.checkMainQueueOpen()

        when (queued) {
            is Queued.HandleInput -> {
                notify(BallastNotification.InputQueued(type, name, queued.input))
            }

            is Queued.RestoreState -> {

            }

            is Queued.ShutDownGracefully -> {

            }
        }

        inputStrategy.enqueue(queued)

        if (await) {
            queued.deferred?.await()
        }
    }

    private fun enqueueQueuedImmediate(queued: Queued<Inputs, Events, State>): ChannelResult<Unit> {
        _internalState.value.status.checkMainQueueOpen()

        when (queued) {
            is Queued.HandleInput -> {
                notifyImmediate(BallastNotification.InputQueued(type, name, queued.input))
            }

            is Queued.RestoreState -> {

            }

            is Queued.ShutDownGracefully -> {

            }
        }

        val result = inputStrategy.tryEnqueue(queued)

        if (result.isFailure || result.isClosed) {
            when (queued) {
                is Queued.HandleInput -> {
                    notifyImmediate(BallastNotification.InputDropped(type, name, queued.input))
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
                safelySetState(queued.state, queued.deferred)
            }

            is Queued.ShutDownGracefully -> {
                gracefullyShutDown(queued.gracePeriod, queued.deferred)
            }
        }
    }

    private suspend fun safelyHandleInput(
        input: Inputs,
        deferred: CompletableDeferred<Unit>?,
        guardian: InputStrategy.Guardian,
        onCancelled: suspend () -> Unit
    ) {
        notify(BallastNotification.InputAccepted(type, name, input))

        try {
            coroutineScope {
                // Create a handler scope to handle the input normally
                val handlerScope = InputHandlerScopeImpl(guardian, this@BallastViewModelImpl)
                with(inputHandler) {
                    handlerScope.handleInput(input)
                }
                handlerScope.close()

                try {
                    notify(BallastNotification.InputHandledSuccessfully(type, name, input))
                } catch (t: Throwable) {
                    notify(BallastNotification.InputHandlerError(type, name, input, t))
                }
                deferred?.complete(Unit)
            }
        } catch (e: CancellationException) {
            // when the coroutine is cancelled for any reason, we must assume the input did not
            // complete and may have left the State in a bad, erm..., state. We should reset it and
            // try to forget that we ever tried to process it in the first place
            notify(BallastNotification.InputCancelled(type, name, input))
            onCancelled()
            deferred?.complete(Unit)
        } catch (e: Throwable) {
            notify(BallastNotification.InputHandlerError(type, name, input, e))
            deferred?.complete(Unit)
        }
    }

    private suspend fun gracefullyShutDownMainQueue() {
        _internalState.update {
            it.copy(
                status = Status.ShuttingDown(
                    stateChangeOpen = true,
                    mainQueueOpen = false,
                    eventsOpen = true,
                    sideJobsOpen = false,
                    sideJobsCancellationOpen = true,
                )
            )
        }
        notify(BallastNotification.ViewModelStatusChanged(type, name, _internalState.value.status))

        // close the main queue and wait for all Inputs to be handled
        inputStrategy.close()
        inputStrategy.flush()
    }

// Events
// ---------------------------------------------------------------------------------------------------------------------

    public fun attachEventHandler(
        handler: EventHandler<Inputs, Events, State>,
        coroutineScope: CoroutineScope = viewModelScope,
    ) {
        val eventHandlerCoroutineScope = coroutineScope +
                uncaughtExceptionHandler +
                eventsDispatcher

        eventHandlerCoroutineScope.launch {
            notify(BallastNotification.EventProcessingStarted(type, name))

            coroutineContext.job.invokeOnCompletion {
                notifyImmediate(BallastNotification.EventProcessingStopped(type, name))
            }

            val eventStrategyScope = EventStrategyScopeImpl(this@BallastViewModelImpl, handler)

            with(eventStrategy) {
                eventStrategyScope.start()
            }
        }
    }

    internal suspend fun enqueueEvent(event: Events, deferred: CompletableDeferred<Unit>?, await: Boolean) {
        _internalState.value.status.checkEventsOpen()
        notify(BallastNotification.EventQueued(type, name, event))
        eventStrategy.enqueue(event)
        if (await && deferred != null) {
            deferred.await()
        }
    }

    internal suspend fun safelyHandleEvent(
        event: Events,
        handler: EventHandler<Inputs, Events, State>
    ) {
        notify(BallastNotification.EventEmitted(type, name, event))
        try {
            coroutineScope {
                val handlerScope = EventHandlerScopeImpl(this@BallastViewModelImpl)
                with(handler) {
                    handlerScope.handleEvent(event)
                }
                handlerScope.ensureUsedCorrectly()
                notify(BallastNotification.EventHandledSuccessfully(type, name, event))
            }
        } catch (e: CancellationException) {
            // ignore
        } catch (e: Throwable) {
            notify(BallastNotification.EventHandlerError(type, name, event, e))
        }
    }

    private suspend fun gracefullyShutDownEvents() {
        _internalState.update {
            it.copy(
                status = Status.ShuttingDown(
                    stateChangeOpen = false,
                    mainQueueOpen = false,
                    eventsOpen = false,
                    sideJobsOpen = false,
                    sideJobsCancellationOpen = false,
                )
            )
        }
        notify(BallastNotification.ViewModelStatusChanged(type, name, _internalState.value.status))

        // close the Events queue and wait for all Events to be handled
        eventStrategy.close()
        eventStrategy.flush()
    }

// States
// ---------------------------------------------------------------------------------------------------------------------

    internal suspend fun getCurrentState(): State {
        return _state.value
    }

    internal suspend fun safelySetState(state: State, deferred: CompletableDeferred<Unit>?) {
        _internalState.value.status.checkStateChangeOpen()
        _state.value = state
        notify(BallastNotification.StateChanged(type, name, getCurrentState()))
        deferred?.complete(Unit)
    }

    internal suspend fun safelyUpdateState(block: (State) -> State) {
        _internalState.value.status.checkStateChangeOpen()
        _state.update(block)
        notify(BallastNotification.StateChanged(type, name, getCurrentState()))
    }

    internal suspend fun safelyUpdateStateAndGet(block: (State) -> State): State {
        _internalState.value.status.checkStateChangeOpen()
        return _state.updateAndGet(block).also {
            notify(BallastNotification.StateChanged(type, name, getCurrentState()))
        }
    }

    internal suspend fun safelyGetAndUpdateState(block: (State) -> State): State {
        _internalState.value.status.checkStateChangeOpen()
        return _state.getAndUpdate(block).also {
            notify(BallastNotification.StateChanged(type, name, getCurrentState()))
        }
    }

// Side Jobs
// ---------------------------------------------------------------------------------------------------------------------

    private val _sideJobsRequestQueue: Channel<SideJobRequest<Inputs, Events, State>> =
        Channel(BUFFERED, BufferOverflow.SUSPEND)
    private val _sideJobsRequestQueueDrained = CompletableDeferred<Unit>()

    private fun startSideJobsInternal() {
        // start sideJobs posted by Inputs
        viewModelScope.launch {
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
        _internalState.value.status.checkSideJobsOpen()
        notifyImmediate(BallastNotification.SideJobQueued(type, name, key))
        _sideJobsRequestQueue.trySend(SideJobRequest.StartOrRestartSideJob(key, block))
    }

    internal fun cancelSideJob(
        key: String,
    ) {
        _internalState.value.status.checkSideJobCancellationOpen()
        _sideJobsRequestQueue.trySend(SideJobRequest.CancelSideJob(key))
    }

    private fun safelyStartOrRestartSideJob(
        request: SideJobRequest.StartOrRestartSideJob<Inputs, Events, State>,
    ) {
        val newState = _internalState.updateAndGet {
            val sideJobMap = it.sideJobs.toMutableMap()

            val sideJobListForKey = sideJobMap.getOrPut(request.key) { SideJobList(request.key) }
            val updatedList = sideJobListForKey.addNewRunningJob(viewModelScope.coroutineContext.job)
            sideJobMap[request.key] = updatedList

            it.copy(sideJobs = sideJobMap.toMap())
        }

        val sideJobListForKey = newState.sideJobs[request.key]!!
        val latestSideJobForKey = sideJobListForKey.currentSideJob!!

        val sideJobCoroutineScope = viewModelScope +
                latestSideJobForKey.job +
                sideJobsDispatcher

        sideJobCoroutineScope.launch {
            notify(
                BallastNotification.SideJobStarted(
                    type,
                    name,
                    key = latestSideJobForKey.key,
                    restartState = latestSideJobForKey.restartState,
                )
            )

            try {
                // run the sideJob, which may never complete
                coroutineScope {
                    val sideJobScope = SideJobScopeImpl(
                        key = latestSideJobForKey.key,
                        restartState = latestSideJobForKey.restartState,
                        sideJobCoroutineScope = this,
                        impl = this@BallastViewModelImpl,
                    )
                    request.block.invoke(sideJobScope)
                }

                // If it does complete normally...

                // mark the sideJob as complete
                completeSideJob(latestSideJobForKey.key, latestSideJobForKey.sideJobId)

                // send a notification that the side job has completed
                notify(
                    BallastNotification.SideJobCompleted(
                        type,
                        name,
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
                notify(
                    BallastNotification.SideJobCancelled(
                        type,
                        name,
                        key = latestSideJobForKey.key,
                        restartState = latestSideJobForKey.restartState,
                    )
                )
            } catch (e: Throwable) {
                // an exception was thrown when executing the sideJob.

                // mark the sideJob as complete
                completeSideJob(latestSideJobForKey.key, latestSideJobForKey.sideJobId)

                // send a notification that the side job was cancelled
                notify(
                    BallastNotification.SideJobError(
                        type,
                        name,
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
        _internalState.value.sideJobs[request.key]?.cancelSideJobs()
    }

    private fun safelyCancelAllSideJobs() {
        _internalState.value.sideJobs.forEach { it.value.cancelSideJobs() }
    }

    private fun completeSideJob(
        key: String,
        id: Int,
    ) {
        _internalState.updateAndGet {
            val sideJobMap = it.sideJobs.toMutableMap()

            val sideJobListForKey = sideJobMap.getOrPut(key) { SideJobList(key) }
            val updatedList = sideJobListForKey.removeCompletedJob(id)
            sideJobMap[key] = updatedList

            it.copy(sideJobs = sideJobMap.toMap())
        }
    }

    private suspend fun gracefullyShutDownSideJobs(gracePeriod: Duration) {
        _internalState.update {
            it.copy(
                status = Status.ShuttingDown(
                    stateChangeOpen = true,
                    mainQueueOpen = true,
                    eventsOpen = true,
                    sideJobsOpen = false,
                    sideJobsCancellationOpen = true,
                )
            )
        }
        notify(BallastNotification.ViewModelStatusChanged(type, name, _internalState.value.status))

        try {
            withTimeout(gracePeriod) {
                // close the sideJobs request queue and wait for all requests to be handled
                _sideJobsRequestQueue.close()
                _sideJobsRequestQueueDrained.await()

                // without forcibly cancelling, wait for all sideJobs to complete
                _internalState.value
                    .sideJobs
                    .flatMap { it.value.runningJobs.map { it.job } }
                    .joinAll()
            }
        } catch (e: Exception) {
            // the sideJobs did not complete during the grace period. Force them all to cancel, then wait for them to
            // complete
            safelyCancelAllSideJobs()
            _internalState.value
                .sideJobs
                .flatMap { it.value.runningJobs.map { it.job } }
                .joinAll()
        }
    }

// Interceptors and Notifications
// ---------------------------------------------------------------------------------------------------------------------

    private val _notificationsQueue: Channel<BallastNotification<Inputs, Events, State>> =
        Channel(BUFFERED, BufferOverflow.SUSPEND)
    private val _notificationsQueueDrained: CompletableDeferred<Unit> = CompletableDeferred()

    private val _notifications: MutableSharedFlow<BallastNotification<Inputs, Events, State>> = MutableSharedFlow()

    private fun startInterceptorsInternal() {
        // send notifications to Interceptors
        interceptors
            .forEach { interceptor ->
                val notificationFlow: Flow<BallastNotification<Inputs, Events, State>> = _notifications
                    .asSharedFlow()
                    .transformWhile {
                        emit(it)

                        val shouldStopProcessingNotifications = when (it) {
                            is BallastNotification.ViewModelStatusChanged -> {
                                it.status != Status.Cleared
                            }

                            else -> true
                        }

                        shouldStopProcessingNotifications
                    }

                with(interceptor) {
                    try {
                        BallastInterceptorScopeImpl(
                            interceptorCoroutineScope = viewModelScope +
                                    uncaughtExceptionHandler +
                                    interceptorDispatcher +
                                    SupervisorJob(viewModelScope.coroutineContext.job),
                            this@BallastViewModelImpl,
                        ).start(notificationFlow)
                    } catch (e: Exception) {
                        notifyImmediate(BallastNotification.InterceptorFailed(type, name, interceptor, e))
                    }
                }
            }

        interceptors
            .forEach { interceptor ->
                notifyImmediate(BallastNotification.InterceptorAttached(type, name, interceptor))
            }
    }

    private fun startProcessingNotificationsInternal() {
        // observe and process Inputs
        viewModelScope.launch {
            _notificationsQueue
                .receiveAsFlow()
                .onEach { _notifications.emit(it) }
                .flowOn(sideJobsDispatcher)
                .onCompletion { _notificationsQueueDrained.complete(Unit) }
                .launchIn(this)
        }
    }

    @Suppress("UNCHECKED_CAST")
    internal suspend fun <I : BallastInterceptor<*, *, *>> getInterceptor(key: BallastInterceptor.Key<I>): I {
        val interceptorsWithKey = interceptors
            .filter {
                if (it.key == null) {
                    false
                } else {
                    it.key === key
                }
            }

        if (interceptorsWithKey.isEmpty()) {
            error("Interceptor with key '$key' is not registered to ViewModel '$name'")
        }

        if (interceptorsWithKey.size > 1) {
            error("Multiple interceptors with key '$key' are registered to ViewModel '$name'")
        }

        return interceptorsWithKey.single() as? I
            ?: error("Interceptor with key '$key' does not match the type of it key")
    }

    internal suspend fun notify(value: BallastNotification<Inputs, Events, State>) {
        _notificationsQueue.send(value)
    }

    private fun notifyImmediate(value: BallastNotification<Inputs, Events, State>) {
        _notificationsQueue.trySend(value)
    }

    private suspend fun gracefullyShutDownNotifications() {
        // close the Notifications queue and wait for all Notifications to be handled
        _notificationsQueue.close()
        _notificationsQueueDrained.await()
    }
}
