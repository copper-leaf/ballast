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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
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

// Internal properties
// ---------------------------------------------------------------------------------------------------------------------

    internal lateinit var viewModelScope: CoroutineScope
    private var started = false

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
        _notifications.tryEmit(BallastNotification.UnhandledError(this@BallastViewModelImpl, e))
    }

// Core MVI pattern API
// ---------------------------------------------------------------------------------------------------------------------

    override fun observeStates(): StateFlow<State> {
        check(started) { "VM is not started!" }
        return outputStates.asStateFlow()
    }

    override suspend fun send(element: Inputs) {
        check(started) { "VM is not started!" }
        _inputs.send(element)
        _notifications.emit(BallastNotification.InputQueued(this@BallastViewModelImpl, element))
    }

    override fun trySend(element: Inputs): ChannelResult<Unit> {
        check(started) { "VM is not started!" }
        val result = _inputs.trySend(element)
        if (result.isFailure) {
            _notifications.tryEmit(BallastNotification.InputDropped(this@BallastViewModelImpl, element))
            result.exceptionOrNull()?.printStackTrace()
        } else {
            _notifications.tryEmit(BallastNotification.InputQueued(this@BallastViewModelImpl, element))
        }
        return result
    }

    public suspend fun attachEventHandler(
        handler: EventHandler<Inputs, Events, State>
    ) {
        _notifications.emit(BallastNotification.EventProcessingStarted(this@BallastViewModelImpl))

        coroutineContext.job.invokeOnCompletion {
            _notifications.tryEmit(BallastNotification.EventProcessingStopped(this@BallastViewModelImpl))
        }

        withContext(uncaughtExceptionHandler) {
            for (event in _events) {
                safelyHandleEvent(event, handler)
            }
        }
    }

    @ExperimentalCoroutinesApi
    public suspend fun awaitSideEffectsCompletion() {
        // run a bust loop until the sideEffects channel is drained
        while (true) {
            if (_sideEffects.isEmpty) break
            yield()
        }

        coroutineScope {
            val dummyNotification = BallastNotification.ViewModelCleared(this@BallastViewModelImpl)
            launch {
                _notifications.emit(dummyNotification)
            }

            _notifications.first { it === dummyNotification }
        }

        // wait for all the sideEffect jobs to complete
        currentSideEffects.values
            .mapNotNull { it.job }
            .let { joinAll(*it.toTypedArray()) }
    }

// ViewModel Lifecycle
// ---------------------------------------------------------------------------------------------------------------------

    public fun start(coroutineScope: CoroutineScope) {
        check(!started) { "VM is already started" }
        viewModelScope = coroutineScope + uncaughtExceptionHandler
        startInternal()
    }

    override fun onCleared() {
        check(started) { "VM is not started!" }
        started = false

        for (value in currentSideEffects.values) {
            value.job?.cancel()
            value.job = null
        }

        // side-effects are already bound by the viewModelScope, and will get cancelled
        // automatically, but we still need to clear the registry
        currentSideEffects.clear()

        _notifications.tryEmit(BallastNotification.ViewModelCleared(this@BallastViewModelImpl))
    }

// Internals
// ---------------------------------------------------------------------------------------------------------------------

    private fun startInternal() {
        started = true

        // updates to current state post a new event with the new state
        viewModelScope.launch {
            _state
                .onEach { state ->
                    _notifications.emit(BallastNotification.StateChanged(this@BallastViewModelImpl, state))
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

            config.inputStrategy.processInputs(
                filteredQueue = filteredInputsFlow,
                acceptQueued = { queued, guardian ->
                    when (queued) {
                        is Queued.HandleInput -> {
                            safelyHandleInput(queued.input, guardian)
                        }
                        is Queued.RestoreState -> TODO("Restoring state from an Interceptor is not yet implemented")
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
                interceptor.start(
                    viewModelScope = viewModelScope,
                    notifications = _notifications.asSharedFlow(),
                    sendToQueue = { _inputs.send(it) }
                )
            }

        _notifications.tryEmit(BallastNotification.ViewModelStarted(this@BallastViewModelImpl))
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
                            this@BallastViewModelImpl,
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
        _notifications.emit(BallastNotification.InputAccepted(this@BallastViewModelImpl, input))

        val stateBeforeCancellation = _state.value
        try {
            coroutineScope {
                // Create a handler scope to handle the input normally
                val handlerScope = InputHandlerScopeImpl<Inputs, Events, State>(
                    _state = _state,
                    guardian = guardian,
                    sendEventToQueue = {
                        _notifications.emit(BallastNotification.EventQueued(this@BallastViewModelImpl, it))
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
                    _notifications.emit(BallastNotification.InputHandledSuccessfully(this@BallastViewModelImpl, input))
                } catch (t: Throwable) {
                    _notifications.emit(BallastNotification.InputHandlerError(this@BallastViewModelImpl, input, t))
                }
            }
        } catch (e: CancellationException) {
            // when the coroutine is cancelled for any reason, we must assume the input did not
            // complete and may have left the State in a bad, erm..., state. We should reset it and
            // try to forget that we ever tried to process it in the first place
            _notifications.emit(BallastNotification.InputCancelled(this@BallastViewModelImpl, input))
            if (config.inputStrategy.rollbackOnCancellation) {
                _state.value = stateBeforeCancellation
            }
        } catch (e: Throwable) {
            _notifications.emit(BallastNotification.InputHandlerError(this@BallastViewModelImpl, input, e))
        }
    }

    private suspend fun safelyHandleEvent(
        event: Events,
        handler: EventHandler<Inputs, Events, State>
    ) {
        _notifications.emit(BallastNotification.EventEmitted(this@BallastViewModelImpl, event))
        try {
            coroutineScope {
                val handlerScope = EventHandlerScopeImpl<Inputs, Events, State>(
                    _inputs = this@BallastViewModelImpl
                )
                with(handler) {
                    handlerScope.handleEvent(event)
                }
                handlerScope.ensureUsedCorrectly()
                _notifications.emit(BallastNotification.EventHandledSuccessfully(this@BallastViewModelImpl, event))
            }
        } catch (e: CancellationException) {
            // ignore
        } catch (e: Throwable) {
            _notifications.emit(BallastNotification.EventHandlerError(this@BallastViewModelImpl, event, e))
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
            SupervisorJob(parent = viewModelScope.coroutineContext[Job])

        sideEffectContainer.job = sideEffectCoroutineScope.launch {
            _notifications
                .emit(BallastNotification.SideEffectStarted(this@BallastViewModelImpl, key, restartState))

            try {
                coroutineScope {
                    val sideEffectScope = SideEffectScopeImpl(
                        _inputs = this@BallastViewModelImpl,
                        _events = _events,
                        currentStateWhenStarted = _state.value,
                        restartState = restartState,
                        coroutineScope = this,
                    )
                    sideEffectContainer.block.invoke(sideEffectScope)
                }
                _notifications
                    .emit(BallastNotification.SideEffectCompleted(this@BallastViewModelImpl, key, restartState))
            } catch (e: CancellationException) {
                // ignore
                _notifications
                    .emit(BallastNotification.SideEffectCancelled(this@BallastViewModelImpl, key, restartState))
            } catch (e: Throwable) {
                _notifications
                    .emit(BallastNotification.SideEffectError(this@BallastViewModelImpl, key, restartState, e))
            }
        }
    }
}
