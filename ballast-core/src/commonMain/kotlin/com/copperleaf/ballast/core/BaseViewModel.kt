package com.copperleaf.ballast.core

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastViewModel
import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.internal.BallastViewModelImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.coroutines.EmptyCoroutineContext

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

    public constructor(
        initialState: State,
        inputHandler: InputHandler<Inputs, Events, State>,
        eventHandler: EventHandler<Inputs, Events, State>,
        interceptor: BallastInterceptor<Inputs, Events, State>,
        coroutineScope: CoroutineScope = CoroutineScope(EmptyCoroutineContext)
    ) : this(
        BallastViewModelImpl(
            DefaultViewModelConfiguration(
                initialState = initialState,
                inputHandler = inputHandler,
                interceptor = interceptor,
            ),
        ),
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
