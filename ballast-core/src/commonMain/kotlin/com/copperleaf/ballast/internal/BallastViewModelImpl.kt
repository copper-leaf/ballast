package com.copperleaf.ballast.internal

import com.copperleaf.ballast.BallastNotification
import com.copperleaf.ballast.BallastViewModel
import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.InputFilter
import com.copperleaf.ballast.InputStrategy
import com.copperleaf.ballast.Queued
import com.copperleaf.ballast.SideJobScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
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
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration

public class BallastViewModelImpl<Inputs : Any, Events : Any, State : Any>(
    internal val type: String,
    private val config: BallastViewModelConfiguration<Inputs, Events, State>,
) : BallastViewModel<Inputs, Events, State>,
    BallastViewModelConfiguration<Inputs, Events, State> by config {

    private enum class Status {
        NotStarted,
        Running,
        ShuttingDown,
        Cleared,
    }

// Internal properties
// ---------------------------------------------------------------------------------------------------------------------

    internal lateinit var viewModelScope: CoroutineScope
    private var status: Status = Status.NotStarted

    private val _inputQueue: Channel<Queued<Inputs, Events, State>> =
        config.inputStrategy.createQueue()

    private val _state: MutableStateFlow<State> =
        MutableStateFlow(initialState)

    private val _events: Channel<Events> =
        Channel(BUFFERED, BufferOverflow.SUSPEND)
    private val _sideJobs: Channel<SideJobRequest<Inputs, Events, State>> =
        Channel(BUFFERED, BufferOverflow.SUSPEND)
    private val _notifications: MutableSharedFlow<BallastNotification<Inputs, Events, State>> =
        MutableSharedFlow(extraBufferCapacity = Int.MAX_VALUE)

    private var currentSideJobs = mutableMapOf<String, RunningSideJob<Inputs, Events, State>>()

    private val uncaughtExceptionHandler = CoroutineExceptionHandler { _, e ->
        _notifications.tryEmit(BallastNotification.UnhandledError(type, name, e))
    }

// Core MVI pattern API
// ---------------------------------------------------------------------------------------------------------------------

    override fun observeStates(): StateFlow<State> {
        checkValidState()
        return _state.asStateFlow()
    }

    override suspend fun send(element: Inputs) {
        enqueueInput(input = element, deferred = null, await = false)
    }

    override suspend fun sendAndAwaitCompletion(element: Inputs) {
        enqueueInput(input = element, deferred = CompletableDeferred(), await = true)
    }

    override fun trySend(element: Inputs): ChannelResult<Unit> {
        return enqueueInputImmediate(input = element, deferred = null)
    }

    public suspend fun attachEventHandler(
        handler: EventHandler<Inputs, Events, State>
    ) {
        _notifications.emit(BallastNotification.EventProcessingStarted(type, name))

        coroutineContext.job.invokeOnCompletion {
            _notifications.tryEmit(BallastNotification.EventProcessingStopped(type, name))
        }

        withContext(uncaughtExceptionHandler) {
            _events
                .receiveAsFlow()
                .onEach { safelyHandleEvent(it, handler) }
                .flowOn(eventsDispatcher)
                .launchIn(this)
        }
    }

    // TODO: rather than watching Jobs or anything else directly, re-implement the Test module to be an Interceptor and
    //   track all its state by watching Notifications
    @ExperimentalCoroutinesApi
    public suspend fun awaitSideJobsCompletion() {
        // run a busy loop until all side-jobs are started
        while (true) {
            if (_sideJobs.isEmpty) break
            yield()
        }

        // wait for all the sideJob jobs to complete
        currentSideJobs.values
            .mapNotNull { it.job }
            .let { joinAll(*it.toTypedArray()) }

        // run a busy loop until all input channels are drained
        while (true) {
            if (_inputQueue.isEmpty) break
            yield()
        }
    }

// ViewModel Lifecycle
// ---------------------------------------------------------------------------------------------------------------------

    public fun start(coroutineScope: CoroutineScope) {
        // check the ViewModel is in a valid state to be started
        when (status) {
            Status.NotStarted -> {
                status = Status.Running
            }

            Status.Running -> {
                error("VM is already started")
            }

            Status.ShuttingDown -> {
                error("VM is already started and is shutting down, it cannot be restarted")
            }

            Status.Cleared -> {
                error("VM is cleared, it cannot be restarted")
            }
        }

        // create the real viewModel's coroutineScope
        viewModelScope = coroutineScope + uncaughtExceptionHandler

        // start processing everything within the ViewModel
        startMainQueueInternal()
        startSideJobsInternal()
        startInterceptorsInternal()

        // notify interceptors that the VM is officially started
        _notifications.tryEmit(BallastNotification.ViewModelStarted(type, name))

        // emit the initial state
        _notifications.tryEmit(BallastNotification.StateChanged(type, name, initialState))

        // set the VM to clear itself upon the cancellation of its coroutine scope
        viewModelScope.coroutineContext.job.invokeOnCompletion {
            onCleared()
        }
    }

    private fun onCleared() {
        when (status) {
            Status.NotStarted -> {
                error("VM is not started!")
            }

            Status.Running -> {
                status = Status.Cleared
            }

            Status.ShuttingDown -> {
                status = Status.Cleared
            }

            Status.Cleared -> {
                error("VM is already cleared")
            }
        }

        for (value in currentSideJobs.values) {
            value.job?.cancel()
            value.job = null
        }

        // side-jobs are already bound by the viewModelScope, and will get cancelled
        // automatically, but we still need to clear the registry
        currentSideJobs.clear()

        _notifications.tryEmit(BallastNotification.ViewModelCleared(type, name))

        _inputQueue.close()
        _events.close()
        _sideJobs.close()
    }

// Internals
// ---------------------------------------------------------------------------------------------------------------------

    private fun checkValidState() {
        when (status) {
            Status.NotStarted -> {
                error("VM is not started!")
            }

            Status.Running -> {
                // OK
            }

            Status.ShuttingDown -> {
                // OK
            }

            Status.Cleared -> {
                error("VM is cleared!")
            }
        }
    }

    internal suspend fun gracefullyShutDown(gracePeriod: Duration, deferred: CompletableDeferred<Unit>?) {
        TODO()
    }

// Main Queue
// ---------------------------------------------------------------------------------------------------------------------

    private fun startMainQueueInternal() {
        // observe and process Inputs
        viewModelScope.launch {
            val filteredInputsFlow = _inputQueue
                .receiveAsFlow()
                .filter { queued -> filterQueued(queued) }
                .flowOn(inputsDispatcher)

            with(config.inputStrategy) {
                InputStrategyScopeImpl(this@BallastViewModelImpl).processInputs(
                    filteredQueue = filteredInputsFlow,
                )
            }
        }
    }

    private suspend fun filterQueued(queued: Queued<Inputs, Events, State>): Boolean {
        when (queued) {
            is Queued.RestoreState -> {
                // when restoring state, always accept the item
                return true
            }

            is Queued.HandleInput -> {
                // when handling an Input, check with the InputFilter to see if it should be accepted
                val currentState = getCurrentState()
                val shouldAcceptInput = filter?.filterInput(currentState, queued.input) ?: InputFilter.Result.Accept

                if (shouldAcceptInput == InputFilter.Result.Reject) {
                    _notifications.emit(BallastNotification.InputRejected(type, name, currentState, queued.input))
                    queued.deferred?.complete(Unit)
                }

                return shouldAcceptInput == InputFilter.Result.Accept
            }

            is Queued.CloseGracefully -> {
                return true
            }
        }
    }

    internal suspend fun enqueueQueued(queued: Queued<Inputs, Events, State>) {
        _inputQueue.send(queued)
    }

    private suspend fun gracefullyShutDownMainQueue() {

    }

// Inputs
// ---------------------------------------------------------------------------------------------------------------------

    internal fun enqueueInputImmediate(input: Inputs, deferred: CompletableDeferred<Unit>?): ChannelResult<Unit> {
        checkValidState()
        _notifications.tryEmit(BallastNotification.InputQueued(type, name, input))
        val result = _inputQueue.trySend(Queued.HandleInput(deferred, input))
        if (result.isFailure || result.isClosed) {
            _notifications.tryEmit(BallastNotification.InputDropped(type, name, input))
        }
        return result
    }

    internal suspend fun enqueueInput(input: Inputs, deferred: CompletableDeferred<Unit>?, await: Boolean) {
        checkValidState()
        _notifications.emit(BallastNotification.InputQueued(type, name, input))
        _inputQueue.send(Queued.HandleInput(deferred, input))
        if (await && deferred != null) {
            deferred.await()
        }
    }

    internal suspend fun safelyHandleInput(
        input: Inputs,
        deferred: CompletableDeferred<Unit>?,
        guardian: InputStrategy.Guardian,
    ) {
        _notifications.emit(BallastNotification.InputAccepted(type, name, input))

        val stateBeforeCancellation = getCurrentState()
        try {
            coroutineScope {
                // Create a handler scope to handle the input normally
                val handlerScope = InputHandlerScopeImpl(guardian, this@BallastViewModelImpl)
                with(inputHandler) {
                    handlerScope.handleInput(input)
                }
                handlerScope.close()

                try {
                    _notifications.emit(BallastNotification.InputHandledSuccessfully(type, name, input))
                } catch (t: Throwable) {
                    _notifications.emit(BallastNotification.InputHandlerError(type, name, input, t))
                }
                deferred?.complete(Unit)
            }
        } catch (e: CancellationException) {
            // when the coroutine is cancelled for any reason, we must assume the input did not
            // complete and may have left the State in a bad, erm..., state. We should reset it and
            // try to forget that we ever tried to process it in the first place
            _notifications.emit(BallastNotification.InputCancelled(type, name, input))
            if (config.inputStrategy.rollbackOnCancellation) {
                safelySetState(stateBeforeCancellation, null)
            }
            deferred?.complete(Unit)
        } catch (e: Throwable) {
            _notifications.emit(BallastNotification.InputHandlerError(type, name, input, e))
            deferred?.complete(Unit)
        }
    }

// Events
// ---------------------------------------------------------------------------------------------------------------------

    internal suspend fun enqueueEvent(event: Events, deferred: CompletableDeferred<Unit>?, await: Boolean) {
        checkValidState()
        _notifications.emit(BallastNotification.EventQueued(type, name, event))
        _events.send(event)
        if (await && deferred != null) {
            deferred.await()
        }
    }

    internal suspend fun safelyHandleEvent(
        event: Events,
        handler: EventHandler<Inputs, Events, State>
    ) {
        _notifications.emit(BallastNotification.EventEmitted(type, name, event))
        try {
            coroutineScope {
                val handlerScope = EventHandlerScopeImpl(this@BallastViewModelImpl)
                with(handler) {
                    handlerScope.handleEvent(event)
                }
                handlerScope.ensureUsedCorrectly()
                _notifications.emit(BallastNotification.EventHandledSuccessfully(type, name, event))
            }
        } catch (e: CancellationException) {
            // ignore
        } catch (e: Throwable) {
            _notifications.emit(BallastNotification.EventHandlerError(type, name, event, e))
        }
    }

    private suspend fun gracefullyShutDownEvents() {

    }

// States
// ---------------------------------------------------------------------------------------------------------------------

    internal suspend fun getCurrentState(): State {
        return _state.value
    }

    internal suspend fun safelySetState(state: State, deferred: CompletableDeferred<Unit>?) {
        checkValidState()
        _state.value = state
        _notifications.emit(BallastNotification.StateChanged(type, name, getCurrentState()))
        deferred?.complete(Unit)
    }

    internal suspend fun safelyUpdateState(block: (State) -> State) {
        checkValidState()
        _state.update(block)
        _notifications.emit(BallastNotification.StateChanged(type, name, getCurrentState()))
    }

    internal suspend fun safelyUpdateStateAndGet(block: (State) -> State): State {
        checkValidState()
        return _state.updateAndGet(block).also {
            _notifications.emit(BallastNotification.StateChanged(type, name, getCurrentState()))
        }
    }

    internal suspend fun safelyGetAndUpdateState(block: (State) -> State): State {
        checkValidState()
        return _state.getAndUpdate(block).also {
            _notifications.emit(BallastNotification.StateChanged(type, name, getCurrentState()))
        }
    }

// Side Jobs
// ---------------------------------------------------------------------------------------------------------------------

    private fun startSideJobsInternal() {
        // start sideJobs posted by Inputs
        viewModelScope.launch {
            _sideJobs
                .receiveAsFlow()
                .onEach { safelyStartSideJob(it) }
                .launchIn(this)
        }
    }

    internal fun enqueueSideJob(
        key: String,
        block: suspend SideJobScope<Inputs, Events, State>.() -> Unit,
    ) {
        checkValidState()
        _notifications.tryEmit(BallastNotification.SideJobQueued(type, name, key))
        _sideJobs.trySend(SideJobRequest(key, block))
    }

    internal suspend fun safelyStartSideJob(
        request: SideJobRequest<Inputs, Events, State>,
    ) {
        val restartState = if (currentSideJobs.containsKey(request.key)) {
            SideJobScope.RestartState.Restarted
        } else {
            SideJobScope.RestartState.Initial
        }

        // if we have a side-job already running, cancel its coroutine scope
        currentSideJobs[request.key]?.let {
            it.job?.cancel()
            it.job = null
        }

        // go through and remove any side-jobs that have completed (either by
        // cancellation or because they finished on their own)
        currentSideJobs.entries
            .filterNot { it.value.job?.isActive == true }
            .map { it.key }
            .forEach { currentSideJobs.remove(it) }

        // launch a new side-job in its own isolated coroutine scope where:
        //   1) it is cancelled when the viewModelScope is cancelled
        //   2) errors are caught by the uncaughtExceptionHandler for crash reporting
        //   3) has a supervisor job, so we can cancel the side-job without cancelling the whole viewModelScope
        //
        // Consumers of this side-job can launch many jobs, and all will be cancelled together when the
        // side-job is restarted or the viewModelScope is cancelled.
        val sideJobContainer = RunningSideJob(
            key = request.key,
            block = request.block,
        )
        currentSideJobs[request.key] = sideJobContainer

        val sideJobCoroutineScope = viewModelScope +
                SupervisorJob(parent = viewModelScope.coroutineContext[Job]) +
                sideJobsDispatcher

        sideJobContainer.job = sideJobCoroutineScope.launch {
            _notifications.emit(
                BallastNotification.SideJobStarted(
                    type,
                    name,
                    request.key,
                    restartState,
                )
            )

            try {
                coroutineScope {
                    val sideJobScope = SideJobScopeImpl(
                        currentStateWhenStarted = getCurrentState(),
                        restartState = restartState,
                        sideJobCoroutineScope = this,
                        impl = this@BallastViewModelImpl,
                    )
                    sideJobContainer.block.invoke(sideJobScope)
                }
                _notifications.emit(BallastNotification.SideJobCompleted(type, name, request.key, restartState))
            } catch (e: CancellationException) {
                // ignore
                _notifications.emit(BallastNotification.SideJobCancelled(type, name, request.key, restartState))
            } catch (e: Throwable) {
                _notifications.emit(BallastNotification.SideJobError(type, name, request.key, restartState, e))
            }
        }
    }

    private suspend fun gracefullyShutDownSideJobs() {

    }

// Interceptors
// ---------------------------------------------------------------------------------------------------------------------

    private fun startInterceptorsInternal() {
        // send notifications to Interceptors
        interceptors
            .forEach { interceptor ->
                val notificationFlow: Flow<BallastNotification<Inputs, Events, State>> = _notifications
                    .asSharedFlow()
                    .transformWhile {
                        emit(it)

                        it !is BallastNotification.ViewModelCleared
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
                        _notifications.tryEmit(BallastNotification.UnhandledError(type, name, e))
                    }
                }
            }
    }
}
