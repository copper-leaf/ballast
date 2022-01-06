package com.copperleaf.ballast.internal

import com.copperleaf.ballast.BallastViewModel
import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.InputFilter
import com.copperleaf.ballast.InputStrategy
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

public class BallastViewModelImpl<Inputs : Any, Events : Any, State : Any>(
    initialState: State,
    private val config: BallastViewModelConfiguration<Inputs, Events, State>,
    private val _inputs: Channel<Inputs> = Channel(Channel.BUFFERED, BufferOverflow.DROP_LATEST),
) : BallastViewModel<Inputs, Events, State>,
    BallastViewModelConfiguration<Inputs, Events, State> by config,
    SendChannel<Inputs> by _inputs {

// Internal properties
// ---------------------------------------------------------------------------------------------------------------------

    private val _state: MutableStateFlow<State> = MutableStateFlow(initialState)
    internal val _events: Channel<Events> = Channel(BUFFERED, BufferOverflow.SUSPEND)
    private val outputStates = MutableStateFlow<State>(initialState)
    internal val uncaughtExceptionHandler = CoroutineExceptionHandler { _, e ->
        interceptor?.onUnhandledError(e)
    }

    private var started = false
    private var sideEffects = mutableMapOf<String?, RunningSideEffect<Inputs, Events, State>>()
    internal lateinit var viewModelScope: CoroutineScope

// Core MVI pattern API
// ---------------------------------------------------------------------------------------------------------------------

    @ExperimentalCoroutinesApi
    public suspend fun awaitSideEffectsCompletion() {
        sideEffects.values
            .mapNotNull { it.job }
            .let { joinAll(*it.toTypedArray()) }
    }

    override fun onCleared() {
        check(started) { "VM is not started!" }
        started = false

        for (value in sideEffects.values) {
            value.job?.cancel()
            value.job = null
        }

        // side-effects are already bound by the viewModelScope, and will get cancelled
        // automatically, but we still need to clear the registry
        sideEffects.clear()
    }

    override fun observeStates(): StateFlow<State> {
        check(started) { "VM is not started!" }
        return outputStates.asStateFlow()
    }

    override suspend fun send(element: Inputs) {
        check(started) { "VM is not started!" }
        _inputs.send(element)
    }

    override fun trySend(element: Inputs): ChannelResult<Unit> {
        check(started) { "VM is not started!" }
        val result = _inputs.trySend(element)
        if (result.isFailure) {
            interceptor?.onInputDropped(element)
            result.exceptionOrNull()?.printStackTrace()
        }
        return result
    }

// Utilities
// ---------------------------------------------------------------------------------------------------------------------

    public fun start(coroutineScope: CoroutineScope) {
        check(!started) { "VM is already started" }
        viewModelScope = coroutineScope + uncaughtExceptionHandler
        startInternal()
    }

    private fun startInternal() {
        started = true

        // updates to current state post a new event with the new state
        viewModelScope.launch {
            _state.collect { state ->
                interceptor?.onStateEmitted(state)
                outputStates.emit(state)
            }
        }

        // observe and process Inputs. Input events are emitted on the main dispatcher, but collected and processed on
        // the io dispatcher. Inputs are collected via the [collectLatest] operator, which cancels currently-running
        // tasks before starting to execute the next input.
        with(config.inputStrategy) {
            viewModelScope.launch {
                val filteredInputsFlow = _inputs
                    .receiveAsFlow()
                    .filter { input ->
                        val shouldAcceptInput = filter?.filterInput(_state.value, input) ?: InputFilter.Result.Accept
                        if (shouldAcceptInput == InputFilter.Result.Reject) {
                            interceptor?.onInputRejected(input)
                        }

                        return@filter shouldAcceptInput == InputFilter.Result.Accept
                    }

                processInputs(
                    filteredInputsFlow,
                    ::safelyHandleInput
                )
            }
        }
    }

    private suspend fun safelyHandleInput(input: Inputs, onCompleted: (InputStrategy.InputResult) -> Unit) {
        interceptor?.onInputAccepted(input)

        val stateBeforeCancellation = _state.value
        try {
            coroutineScope {
                // Create a handler scope to handle the input normally
                val handlerScope = InputHandlerScopeImpl<Inputs, Events, State>(
                    _state = _state,
                    _events = _events,
                )
                with(inputHandler) {
                    handlerScope.handleInput(input)
                }

                // Close the normal scope to prevent unwanted state updates from side-effects, and
                // collect side-effects that should be started or restarted
                val sideEffectsFromInput = handlerScope.close()

                if (sideEffectsFromInput.isNotEmpty()) {
                    sideEffectsFromInput.forEach { sideEffect ->
                        val actualSideEffectKey = sideEffect.key

                        // if we have a side-effect already running, cancel its coroutine scope
                        sideEffects[actualSideEffectKey]?.let {
                            it.job?.cancel()
                            it.job = null
                            it.sideEffect.onRestarted()
                        }

                        // go through and remove any side-effects that have completed (either by
                        // cancellation or because they finished on their own)
                        sideEffects.entries
                            .filterNot { it.value.job?.isActive == true }
                            .map { it.key }
                            .forEach { sideEffects.remove(it) }

                        // launch a new side effect in its own isolated coroutine scope where:
                        //   1) it is cancelled when the viewModelScope is cancelled
                        //   2) errors are caught by the uncaughtExceptionHandler for crash reporting
                        //   3) has a supervisor job, so we can cancel the side-effect without cancelling the whole viewModelScope
                        //
                        // Consumers of this side-effect can launch many jobs, and all will be cancelled together when the
                        // side-effect is restarted or the viewModelScope is cancelled.
                        sideEffects[actualSideEffectKey] = RunningSideEffect(
                            sideEffect = sideEffect,
                            coroutineScope = viewModelScope +
                                SupervisorJob(parent = viewModelScope.coroutineContext[Job]),
                            scope = SideEffectScopeImpl(
                                this@BallastViewModelImpl,
                                _events
                            ),
                        ).also { it.start(_state.value) }
                    }
                }

                try {
                    onCompleted(handlerScope.getResult())
                    interceptor?.onInputHandledSuccessfully(input)
                } catch (t: Throwable) {
                    interceptor?.onInputHandlerError(input, t)
                }
            }
        } catch (e: CancellationException) {
            // when the coroutine is cancelled for any reason, we must assume the input did not
            // complete and may have left the State in a bad, erm..., state. We should reset it and
            // try to forget that we ever tried to process it in the first place
            interceptor?.onInputCancelled(input)
            if (config.inputStrategy.rollbackOnCancellation) {
                _state.value = stateBeforeCancellation
            }
        } catch (e: Throwable) {
            interceptor?.onInputHandlerError(input, e)
        }
    }

    public suspend fun attachEventHandler(
        handler: EventHandler<Inputs, Events, State>
    ) {
        coroutineContext.job.invokeOnCompletion {
            interceptor?.onEventProcessingStopped()
        }

        withContext(uncaughtExceptionHandler) {
            interceptor?.onEventProcessingStarted()
            for (event in _events) {
                interceptor?.onEventEmitted(event)
                safelyHandleEvent(event, handler)
            }
        }
    }

    internal suspend fun safelyHandleEvent(
        event: Events,
        handler: EventHandler<Inputs, Events, State>
    ) {
        try {
            coroutineScope {
                val handlerScope = EventHandlerScopeImpl<Inputs, Events, State>(
                    _inputs = this@BallastViewModelImpl
                )
                with(handler) {
                    handlerScope.handleEvent(event)
                }
                handlerScope.ensureUsedCorrectly()
            }
        } catch (e: CancellationException) {
            // ignore
        } catch (e: Throwable) {
            interceptor?.onEventHandlerError(event, e)
        }
    }
}
