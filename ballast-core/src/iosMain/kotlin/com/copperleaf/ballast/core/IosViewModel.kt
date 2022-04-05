package com.copperleaf.ballast.core

import com.copperleaf.ballast.BallastViewModel
import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.internal.BallastViewModelImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

public fun interface Closeable {
    public fun close()
}

public open class IosViewModel<Inputs : Any, Events : Any, State : Any> private constructor(
    private val impl: BallastViewModelImpl<Inputs, Events, State>,
    coroutineScope: CoroutineScope,
) : BallastViewModel<Inputs, Events, State> by impl, Closeable {

    final override val type: String = "IosViewModel"
    public val initialState: State = impl.initialState

    public constructor(
        config: BallastViewModelConfiguration<Inputs, Events, State>,
        coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
    ) : this(
        BallastViewModelImpl(config),
        coroutineScope
    )

    init {
        impl.start(coroutineScope) { this@IosViewModel }
    }

    /**
     * Observe the changes to state and emitted events from an iOS View. This is typically called from viewWillAppear
     * or viewDidAppear, and cleared in viewWillDisappear.
     */
    public fun onEachState(
        eventHandler: EventHandler<Inputs, Events, State>,
        onStateChanged: (State) -> Unit,
    ): Closeable {
        val statesJob = impl.viewModelScope.launch {
            impl.observeStates().collect { onStateChanged(it) }
        }
        val eventsJob = impl.viewModelScope.launch {
            impl.attachEventHandler(eventHandler)
        }

        return Closeable {
            statesJob.cancel()
            eventsJob.cancel()
        }
    }

    override fun close() {
        impl.viewModelScope.cancel()
    }
}
