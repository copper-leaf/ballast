package com.copperleaf.ballast.core

import com.copperleaf.ballast.BallastViewModel
import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.internal.BallastViewModelImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.coroutines.EmptyCoroutineContext

public class IosViewModel<Inputs : Any, Events : Any, State : Any> private constructor(
    private val impl: BallastViewModelImpl<Inputs, Events, State>,
    coroutineScope: CoroutineScope,
    private val handler: EventHandler<Inputs, Events, State>,
) : BallastViewModel<Inputs, Events, State> by impl {

    public constructor(
        initialState: State,
        config: BallastViewModelConfiguration<Inputs, Events, State>,
        handler: EventHandler<Inputs, Events, State>,
    ) : this(
        BallastViewModelImpl(initialState, config),
        CoroutineScope(EmptyCoroutineContext),
        handler,
    )

    init {
        impl.start(coroutineScope)
    }

    /**
     * Observe the changes to state and emitted events from an iOS ViewController. Corresponds to `onStart` in Android
     */
    public fun onViewWillAppear(onStateChanged: (State) -> Unit) {
        impl.viewModelScope.launch {
            impl.observeStates().collect { onStateChanged(it) }
        }
        impl.viewModelScope.launch {
            impl.attachEventHandler(handler)
        }
    }

    /**
     * Cancel state and events observers from [onViewWillAppear]. Corresponds to `onStop` in Android
     */
    public fun onViewWillDisappear() {
    }

    /**
     * Cancel entire coroutine scope and prevent it from being resumed. Corresponds to `onDestroy` in Android
     */
    public fun onViewDestroyed() {
        impl.onCleared()
    }
}
