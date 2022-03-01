package com.copperleaf.ballast.core

import com.copperleaf.ballast.BallastViewModel
import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.internal.BallastViewModelImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.coroutines.EmptyCoroutineContext

@ExperimentalCoroutinesApi
public open class BaseViewModel<Inputs : Any, Events : Any, State : Any> private constructor(
    private val impl: BallastViewModelImpl<Inputs, Events, State>,
    private val eventHandler: EventHandler<Inputs, Events, State>,
    coroutineScope: CoroutineScope
) : BallastViewModel<Inputs, Events, State> by impl {

    public constructor(
        config: BallastViewModelConfiguration<Inputs, Events, State>,
        eventHandler: EventHandler<Inputs, Events, State>,
        coroutineScope: CoroutineScope = CoroutineScope(EmptyCoroutineContext)
    ) : this(
        BallastViewModelImpl(config),
        eventHandler,
        coroutineScope,
    )

    init {
        impl.start(coroutineScope)
        impl.viewModelScope.launch {
            impl.attachEventHandler(eventHandler)
        }
    }

    public override fun onCleared() {
        impl.viewModelScope.cancel()
        impl.onCleared()
    }
}
