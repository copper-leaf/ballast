package com.copperleaf.ballast.core

import com.copperleaf.ballast.BallastViewModel
import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.internal.BallastViewModelImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.coroutines.EmptyCoroutineContext

public open class IosViewModel<Inputs : Any, Events : Any, State : Any> private constructor(
    private val impl: BallastViewModelImpl<Inputs, Events, State>,
    coroutineScope: CoroutineScope,
) : BallastViewModel<Inputs, Events, State> by impl {

    final override val type: String = "IosViewModel"

    public constructor(
        config: BallastViewModelConfiguration<Inputs, Events, State>,
        coroutineScope: CoroutineScope = CoroutineScope(EmptyCoroutineContext),
    ) : this(
        BallastViewModelImpl(config),
        coroutineScope
    )

    init {
        impl.start(coroutineScope) { this@IosViewModel }
    }

    /**
     * Observe the changes to state and emitted events from an iOS ViewController. Corresponds to `onStart` in Android
     */
    public fun onViewWillAppear(
        eventHandler: EventHandler<Inputs, Events, State>,
        onStateChanged: (State) -> Unit,
    ) {
        impl.viewModelScope.launch {
            impl.observeStates().collect { onStateChanged(it) }
        }
        impl.viewModelScope.launch {
            impl.attachEventHandler(eventHandler)
        }
    }
}
