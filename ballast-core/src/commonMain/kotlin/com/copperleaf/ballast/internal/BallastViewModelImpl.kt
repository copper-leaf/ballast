package com.copperleaf.ballast.internal

import com.copperleaf.ballast.BallastNotification
import com.copperleaf.ballast.BallastViewModel
import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.InputFilter
import com.copperleaf.ballast.InputStrategy
import com.copperleaf.ballast.Queued
import com.copperleaf.ballast.SideEffectScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.channels.ChannelResult
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import kotlin.coroutines.coroutineContext

public class BallastViewModelImpl<Inputs : Any, Events : Any, State : Any>(
    private val config: BallastViewModelConfiguration<Inputs, Events, State>,
    private val _inputs: Channel<Inputs> = config.inputStrategy.createQueue(),
) : BallastViewModel<Inputs, Events, State>,
    BallastViewModelConfiguration<Inputs, Events, State> by config,
    SendChannel<Inputs> by _inputs {

    override val type: String = "BallastViewModelImpl"

// Internal properties
// ---------------------------------------------------------------------------------------------------------------------

    internal lateinit var viewModelScope: CoroutineScope
    private var started = false
    private var cleared = false
    private lateinit var host: () -> BallastViewModel<Inputs, Events, State>

    private val _restoreState: Channel<Queued.RestoreState<Inputs, Events, State>> =
        config.inputStrategy.createQueue()

    private val _state: MutableStateFlow<State> =
        MutableStateFlow(initialState)
    private val outputStates: MutableStateFlow<State> =
        MutableStateFlow(initialState)

    private val _events: Channel<Events> =
        Channel(BUFFERED, BufferOverflow.SUSPEND)
    private val _sideEffects: Channel<SideEffectRequest<Inputs, Events, State>> =
        Channel(BUFFERED, BufferOverflow.SUSPEND)
    private val _notifications: MutableSharedFlow<BallastNotification<Inputs, Events, State>> =
        MutableSharedFlow(extraBufferCapacity = Int.MAX_VALUE)

    private var currentSideEffects = mutableMapOf<String, RunningSideEffect<Inputs, Events, State>>()

    private val uncaughtExceptionHandler = CoroutineExceptionHandler { _, e ->
        _notifications.tryEmit(BallastNotification.UnhandledError(host(), e))
    }

// Core MVI pattern API
// ---------------------------------------------------------------------------------------------------------------------

    override fun observeStates(): StateFlow<State> {
        checkValidState()
        return outputStates.asStateFlow()
    }

    override suspend fun send(element: Inputs) {
        checkValidState()
        _notifications.emit(BallastNotification.InputQueued(host(), element))
        _inputs.send(element)
    }

    override fun trySend(element: Inputs): ChannelResult<Unit> {
        checkValidState()
        _notifications.tryEmit(BallastNotification.InputQueued(host(), element))
        val result = _inputs.trySend(element)
        if (result.isFailure) {
            _notifications.tryEmit(BallastNotification.InputDropped(host(), element))
            result.exceptionOrNull()?.printStackTrace()
        }
        return result
    }

    public suspend fun attachEventHandler(
        handler: EventHandler<Inputs, Events, State>
    ) {
        _notifications.emit(BallastNotification.EventProcessingStarted(host()))

        coroutineContext.job.invokeOnCompletion {
            _notifications.tryEmit(BallastNotification.EventProcessingStopped(host()))
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
    public suspend fun awaitSideEffectsCompletion() {
        // run a busy loop until all side effects are started
        while (true) {
            if (_sideEffects.isEmpty) break
            yield()
        }

        // wait for all the sideEffect jobs to complete
        currentSideEffects.values
            .mapNotNull { it.job }
            .let { joinAll(*it.toTypedArray()) }

        // run a busy loop until all input channels are drained
        while (true) {
            if (_inputs.isEmpty) break
            if (_restoreState.isEmpty) break
            yield()
        }
    }

// ViewModel Lifecycle
// ---------------------------------------------------------------------------------------------------------------------

    public fun start(
        coroutineScope: CoroutineScope,
        getHost: () -> BallastViewModel<Inputs, Events, State>,
    ) {
        check(!started) { "VM is already started" }
        check(!cleared) { "VM is cleared, it cannot be restarted" }
        started = true
        host = getHost
        viewModelScope = coroutineScope + uncaughtExceptionHandler

        startInternal()

        viewModelScope.coroutineContext.job.invokeOnCompletion {
            onCleared()
        }
    }

    private fun onCleared() {
        check(started) { "VM is not started!" }
        check(!cleared) { "VM is already cleared" }
        started = false
        cleared = true

        for (value in currentSideEffects.values) {
            value.job?.cancel()
            value.job = null
        }

        // side-effects are already bound by the viewModelScope, and will get cancelled
        // automatically, but we still need to clear the registry
        currentSideEffects.clear()

        _notifications.tryEmit(BallastNotification.ViewModelCleared(host()))
    }

// Internals
// ---------------------------------------------------------------------------------------------------------------------

    private fun checkValidState() {
        check(started) { "VM is not started!" }
        check(!cleared) { "VM is cleared!" }
    }

    private fun startInternal() {
        // updates to current state post a new event with the new state
        viewModelScope.launch {
            _state
                .onEach { state ->
                    _notifications.emit(BallastNotification.StateChanged(host(), state))
                    outputStates.emit(state)
                }
                .launchIn(this)
        }

        // observe and process Inputs
        viewModelScope.launch {
            val filteredInputsFlow = _inputs
                .receiveAsFlow()
                .map { input -> Queued.HandleInput<Inputs, Events, State>(input) }
                .filter { queued -> filterQueued(queued) }

            val combinedInputsAndStates: Flow<Queued<Inputs, Events, State>> = merge(
                filteredInputsFlow,
                _restoreState.receiveAsFlow()
            ).flowOn(inputsDispatcher)

            config.inputStrategy.processInputs(
                filteredQueue = combinedInputsAndStates,
                acceptQueued = { queued, guardian ->
                    when (queued) {
                        is Queued.HandleInput -> {
                            safelyHandleInput(queued.input, guardian)
                        }
                        is Queued.RestoreState -> {
                            _state.value = queued.state
                        }
                    }
                }
            )
        }

        // start sideEffects posted by Inputs
        viewModelScope.launch {
            _sideEffects
                .receiveAsFlow()
                .onEach { safelyStartSideEffect(it.key, it.block) }
                .launchIn(this)
        }

        // send notifications to Interceptors
        interceptors
            .forEach { interceptor ->
                val notificationFlow: Flow<BallastNotification<Inputs, Events, State>> = _notifications
                    .asSharedFlow()
                    .transformWhile {
                        emit(it)

                        it !is BallastNotification.ViewModelCleared
                    }

                val interceptorScope = BallastInterceptorScopeImpl<Inputs, Events, State>(
                    logger = logger,
                    hostViewModelName = host().name,
                    viewModelScope = viewModelScope +
                        uncaughtExceptionHandler +
                        interceptorDispatcher,
                    sendQueuedToViewModel = {
                        when (it) {
                            is Queued.HandleInput -> {
                                _inputs.send(it.input)
                            }
                            is Queued.RestoreState -> {
                                _restoreState.send(it)
                            }
                        }
                    }
                )

                with(interceptor) {
                    interceptorScope.start(notificationFlow)
                }
            }

        _notifications.tryEmit(BallastNotification.ViewModelStarted(host()))
    }

    private suspend fun filterQueued(queued: Queued<Inputs, Events, State>): Boolean {
        when (queued) {
            is Queued.RestoreState -> {
                // when restoring state, always accept the item
                return true
            }
            is Queued.HandleInput -> {
                // when handling an Input, check with the InputFilter to see if it should be accepted
                val currentState = _state.value
                val shouldAcceptInput = filter?.filterInput(currentState, queued.input) ?: InputFilter.Result.Accept

                if (shouldAcceptInput == InputFilter.Result.Reject) {
                    _notifications.emit(
                        BallastNotification.InputRejected(
                            host(),
                            currentState,
                            queued.input,
                        )
                    )
                }

                return shouldAcceptInput == InputFilter.Result.Accept
            }
        }
    }

    private suspend fun safelyHandleInput(
        input: Inputs,
        guardian: InputStrategy.Guardian,
    ) {
        _notifications.emit(BallastNotification.InputAccepted(host(), input))

        val stateBeforeCancellation = _state.value
        try {
            coroutineScope {
                // Create a handler scope to handle the input normally
                val handlerScope = InputHandlerScopeImpl<Inputs, Events, State>(
                    logger = logger,
                    _state = _state,
                    guardian = guardian,
                    sendEventToQueue = {
                        _notifications.emit(BallastNotification.EventQueued(host(), it))
                        _events.send(it)
                    }
                )
                with(inputHandler) {
                    handlerScope.handleInput(input)
                }

                // Close the normal scope to prevent unwanted state updates from side-effects, and
                // collect side-effects that should be started or restarted
                val sideEffectsFromInput = handlerScope.close()
                sideEffectsFromInput.forEach { sideEffect ->
                    _sideEffects.send(sideEffect)
                }

                try {
                    _notifications.emit(BallastNotification.InputHandledSuccessfully(host(), input))
                } catch (t: Throwable) {
                    _notifications.emit(BallastNotification.InputHandlerError(host(), input, t))
                }
            }
        } catch (e: CancellationException) {
            // when the coroutine is cancelled for any reason, we must assume the input did not
            // complete and may have left the State in a bad, erm..., state. We should reset it and
            // try to forget that we ever tried to process it in the first place
            _notifications.emit(BallastNotification.InputCancelled(host(), input))
            if (config.inputStrategy.rollbackOnCancellation) {
                _state.value = stateBeforeCancellation
            }
        } catch (e: Throwable) {
            _notifications.emit(BallastNotification.InputHandlerError(host(), input, e))
        }
    }

    private suspend fun safelyHandleEvent(
        event: Events,
        handler: EventHandler<Inputs, Events, State>
    ) {
        _notifications.emit(BallastNotification.EventEmitted(host(), event))
        try {
            coroutineScope {
                val handlerScope = EventHandlerScopeImpl<Inputs, Events, State>(
                    logger = logger,
                    _inputs = host()
                )
                with(handler) {
                    handlerScope.handleEvent(event)
                }
                handlerScope.ensureUsedCorrectly()
                _notifications.emit(BallastNotification.EventHandledSuccessfully(host(), event))
            }
        } catch (e: CancellationException) {
            // ignore
        } catch (e: Throwable) {
            _notifications.emit(BallastNotification.EventHandlerError(host(), event, e))
        }
    }

    private suspend fun safelyStartSideEffect(
        key: String,
        block: suspend SideEffectScope<Inputs, Events, State>.() -> Unit,
    ) {
        val restartState = if (currentSideEffects.containsKey(key)) {
            SideEffectScope.RestartState.Restarted
        } else {
            SideEffectScope.RestartState.Initial
        }

        // if we have a side-effect already running, cancel its coroutine scope
        currentSideEffects[key]?.let {
            it.job?.cancel()
            it.job = null
        }

        // go through and remove any side-effects that have completed (either by
        // cancellation or because they finished on their own)
        currentSideEffects.entries
            .filterNot { it.value.job?.isActive == true }
            .map { it.key }
            .forEach { currentSideEffects.remove(it) }

        // launch a new side effect in its own isolated coroutine scope where:
        //   1) it is cancelled when the viewModelScope is cancelled
        //   2) errors are caught by the uncaughtExceptionHandler for crash reporting
        //   3) has a supervisor job, so we can cancel the side-effect without cancelling the whole viewModelScope
        //
        // Consumers of this side-effect can launch many jobs, and all will be cancelled together when the
        // side-effect is restarted or the viewModelScope is cancelled.
        val sideEffectContainer = RunningSideEffect(
            key = key,
            block = block,
        )
        currentSideEffects[key] = sideEffectContainer

        val sideEffectCoroutineScope = viewModelScope +
            SupervisorJob(parent = viewModelScope.coroutineContext[Job]) +
            sideEffectsDispatcher

        sideEffectContainer.job = sideEffectCoroutineScope.launch {
            _notifications
                .emit(BallastNotification.SideEffectStarted(host(), key, restartState))

            try {
                coroutineScope {
                    val sideEffectScope = SideEffectScopeImpl(
                        logger = logger,
                        _inputs = host(),
                        _events = _events,
                        currentStateWhenStarted = _state.value,
                        restartState = restartState,
                        coroutineScope = this,
                    )
                    sideEffectContainer.block.invoke(sideEffectScope)
                }
                _notifications
                    .emit(BallastNotification.SideEffectCompleted(host(), key, restartState))
            } catch (e: CancellationException) {
                // ignore
                _notifications
                    .emit(BallastNotification.SideEffectCancelled(host(), key, restartState))
            } catch (e: Throwable) {
                _notifications
                    .emit(BallastNotification.SideEffectError(host(), key, restartState, e))
            }
        }
    }
}
