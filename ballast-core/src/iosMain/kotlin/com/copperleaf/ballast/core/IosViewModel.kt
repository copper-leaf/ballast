package com.copperleaf.ballast.core

import com.copperleaf.ballast.BallastViewModel
import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.internal.BallastViewModelImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

public open class IosViewModel<Inputs : Any, Events : Any, State : Any> private constructor(
    private val impl: BallastViewModelImpl<Inputs, Events, State>,
    coroutineScope: CoroutineScope,
) : BallastViewModel<Inputs, Events, State> by impl {

    public val initialState: State = impl.initialState

    public constructor(
        config: BallastViewModelConfiguration<Inputs, Events, State>,
        coroutineScope: CoroutineScope = MainScope(),
    ) : this(
        BallastViewModelImpl("IosViewModel", config),
        coroutineScope
    )

    init {
        impl.start(coroutineScope)
    }

    public val stateCallbacks: FlowAdapter<State> by lazy {
        FlowAdapter(impl.viewModelScope, observeStates())
    }

    public fun attachEventHandler(
        handler: EventHandler<Inputs, Events, State>
    ): Canceller {
        return JobCanceller(
            impl.viewModelScope.launch {
                impl.attachEventHandler(handler)
            }
        )
    }

    public fun close() {
        impl.viewModelScope.cancel()
    }
}
